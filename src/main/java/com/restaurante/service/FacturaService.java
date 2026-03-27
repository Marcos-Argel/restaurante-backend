package com.restaurante.service;

import com.restaurante.dto.FacturaDTO;
import com.restaurante.entity.*;
import com.restaurante.exception.*;
import com.restaurante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoCajaRepository turnoCajaRepository;
    private final MesaRepository mesaRepository;

    @Transactional
    public FacturaDTO.Response crearFactura(FacturaDTO.CrearFacturaRequest request, String emailUsuario) {
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", request.getPedidoId()));

        if (pedido.getEstado() == Pedido.EstadoPedido.PAGADO) {
            throw new BusinessException("El pedido ya fue pagado");
        }
        if (pedido.getEstado() == Pedido.EstadoPedido.CANCELADO) {
            throw new BusinessException("El pedido fue cancelado");
        }
        if (facturaRepository.findByPedidoId(pedido.getId()).isPresent()) {
            throw new BusinessException("El pedido ya tiene una factura generada");
        }

        Usuario cajero = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        BigDecimal subtotal = pedido.getSubtotal();
        BigDecimal descuento = request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO;
        BigDecimal propina = request.getPropina() != null ? request.getPropina() : BigDecimal.ZERO;
        BigDecimal pctImpuesto = request.getImpuestoPorcentaje() != null ? request.getImpuestoPorcentaje() : BigDecimal.ZERO;

        BigDecimal baseConDescuento = subtotal.subtract(descuento);
        BigDecimal impuesto = baseConDescuento.multiply(pctImpuesto).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal montoTotal = baseConDescuento.add(impuesto).add(propina);
        BigDecimal cambio = request.getMontoPagado().subtract(montoTotal);

        if (cambio.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El monto pagado es insuficiente");
        }

        Factura factura = Factura.builder()
                .numeroFactura(generarNumeroFactura())
                .pedido(pedido)
                .clienteNombre(request.getClienteNombre())
                .clienteRucNit(request.getClienteRucNit())
                .subtotal(subtotal)
                .impuesto(impuesto)
                .descuento(descuento)
                .propina(propina)
                .montoTotal(montoTotal)
                .metodoPago(Factura.MetodoPago.valueOf(request.getMetodoPago()))
                .montoPagado(request.getMontoPagado())
                .cambio(cambio)
                .estado(Factura.EstadoFactura.EMITIDA)
                .fecha(LocalDateTime.now())
                .usuario(cajero)
                .build();

        if (request.getTurnoId() != null) {
            TurnoCaja turno = turnoCajaRepository.findById(request.getTurnoId()).orElse(null);
            factura.setTurno(turno);
        }

        factura = facturaRepository.save(factura);

        // Marcar pedido como PAGADO y liberar mesa
        pedido.setEstado(Pedido.EstadoPedido.PAGADO);
        pedido.setModificadoPor(cajero);
        pedido.setFechaModificacion(LocalDateTime.now());
        pedidoRepository.save(pedido);

        if (pedido.getMesa() != null) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(Mesa.EstadoMesa.LIBRE);
            mesaRepository.save(mesa);
        }

        return toResponse(factura);
    }

    public List<FacturaDTO.Response> listarHoy() {
        return facturaRepository.findFacturasHoy().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<FacturaDTO.Response> listarTodas() {
        return facturaRepository.findAllByOrderByFechaDesc().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public FacturaDTO.Response buscarPorId(Long id) {
        return toResponse(facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", id)));
    }

    @Transactional
    public FacturaDTO.Response anularFactura(Long id, String emailUsuario) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", id));
        if (factura.getEstado() == Factura.EstadoFactura.ANULADA) {
            throw new BusinessException("La factura ya está anulada");
        }
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();
        factura.setEstado(Factura.EstadoFactura.ANULADA);
        factura.setModificadoPor(usuario);
        factura.setFechaModificacion(LocalDateTime.now());
        return toResponse(facturaRepository.save(factura));
    }

    private String generarNumeroFactura() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "F-" + timestamp;
    }

    private FacturaDTO.Response toResponse(Factura f) {
        FacturaDTO.Response r = new FacturaDTO.Response();
        r.setId(f.getId());
        r.setNumeroFactura(f.getNumeroFactura());
        r.setPedidoNumero(f.getPedido().getNumeroPedido());
        r.setClienteNombre(f.getClienteNombre());
        r.setSubtotal(f.getSubtotal());
        r.setImpuesto(f.getImpuesto());
        r.setDescuento(f.getDescuento());
        r.setPropina(f.getPropina());
        r.setMontoTotal(f.getMontoTotal());
        r.setMetodoPago(f.getMetodoPago().name());
        r.setMontoPagado(f.getMontoPagado());
        r.setCambio(f.getCambio());
        r.setEstado(f.getEstado().name());
        r.setFecha(f.getFecha() != null ? f.getFecha().toString() : null);
        r.setCajero(f.getUsuario().getNombre());

        if (f.getPedido().getItems() != null) {
            r.setItems(f.getPedido().getItems().stream().map(item -> {
                FacturaDTO.ItemResponse ir = new FacturaDTO.ItemResponse();
                ir.setProducto(item.getProducto() != null ? item.getProducto().getNombre() : "Producto");
                ir.setCantidad(item.getCantidad());
                ir.setPrecioUnitario(item.getPrecioUnitario());
                ir.setSubtotal(item.getSubtotal());
                return ir;
            }).collect(Collectors.toList()));
        }

        return r;
    }
}
