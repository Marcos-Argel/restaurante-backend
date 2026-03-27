package com.restaurante.service;

import com.restaurante.dto.PedidoDTO;
import com.restaurante.entity.*;
import com.restaurante.exception.*;
import com.restaurante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioRepository inventarioRepository;
    private final ProductoIngredienteRepository productoIngredienteRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Transactional
    public PedidoDTO.Response crearPedido(PedidoDTO.CrearPedidoRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(generarNumeroPedido());
        pedido.setUsuario(usuario);
        pedido.setTipoPedido(Pedido.TipoPedido.valueOf(request.getTipoPedido()));
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido.setClienteNombre(request.getClienteNombre());
        pedido.setClienteTelefono(request.getClienteTelefono());
        pedido.setClienteDireccion(request.getClienteDireccion());
        pedido.setNotasEspeciales(request.getNotasEspeciales());

        if (request.getMesaId() != null) {
            Mesa mesa = mesaRepository.findById(request.getMesaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mesa", request.getMesaId()));
            pedido.setMesa(mesa);
            mesa.setEstado(Mesa.EstadoMesa.OCUPADA);
            mesaRepository.save(mesa);
        }

        pedido = pedidoRepository.save(pedido);

        BigDecimal total = BigDecimal.ZERO;
        List<DetallePedido> detalles = new ArrayList<>();

        for (PedidoDTO.ItemPedido item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", item.getProductoId()));

            if (producto.getEstado() == Producto.EstadoProducto.INACTIVO) {
                throw new BusinessException("El producto " + producto.getNombre() + " no está disponible");
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .notas(item.getNotas())
                    .estado(DetallePedido.EstadoItem.PENDIENTE)
                    .build();
            detalles.add(detallePedidoRepository.save(detalle));
        }

        pedido.setSubtotal(total);
        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        return toResponse(pedido, detalles);
    }

    @Transactional
    public PedidoDTO.Response cambiarEstado(Long pedidoId, String nuevoEstado, String emailUsuario) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", pedidoId));

        Pedido.EstadoPedido estado = Pedido.EstadoPedido.valueOf(nuevoEstado);
        Pedido.EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(estado);

        // Si pasa a EN_PREPARACION, descontar inventario
        if (estado == Pedido.EstadoPedido.EN_PREPARACION && estadoAnterior == Pedido.EstadoPedido.PENDIENTE) {
            descontarInventario(pedido);
            pedido.setFechaPreparacion(LocalDateTime.now());
        }

        // Si se entrega, marcar fecha
        if (estado == Pedido.EstadoPedido.SERVIDO) {
            pedido.setFechaEntrega(LocalDateTime.now());
            // Marcar ítems como SERVIDO
            List<DetallePedido> items = detallePedidoRepository.findByPedidoId(pedidoId);
            items.forEach(i -> i.setEstado(DetallePedido.EstadoItem.SERVIDO));
            detallePedidoRepository.saveAll(items);
        }

        // Si se cancela y estaba en preparación, devolver inventario
        if (estado == Pedido.EstadoPedido.CANCELADO && estadoAnterior == Pedido.EstadoPedido.EN_PREPARACION) {
            devolverInventario(pedido);
        }

        // Si se cancela o paga, liberar mesa
        if (estado == Pedido.EstadoPedido.CANCELADO || estado == Pedido.EstadoPedido.PAGADO) {
            if (pedido.getMesa() != null) {
                Mesa mesa = pedido.getMesa();
                mesa.setEstado(Mesa.EstadoMesa.LIBRE);
                mesaRepository.save(mesa);
            }
        }

        Usuario modificador = usuarioRepository.findByEmail(emailUsuario).orElse(null);
        pedido.setModificadoPor(modificador);
        pedido.setFechaModificacion(LocalDateTime.now());
        pedidoRepository.save(pedido);

        List<DetallePedido> detalles = detallePedidoRepository.findByPedidoId(pedidoId);
        return toResponse(pedido, detalles);
    }

    private void descontarInventario(Pedido pedido) {
        List<DetallePedido> items = detallePedidoRepository.findByPedidoId(pedido.getId());
        for (DetallePedido item : items) {
            if (!item.getProducto().getEsPreparado()) continue;
            List<ProductoIngrediente> receta = productoIngredienteRepository.findByProductoId(item.getProducto().getId());
            for (ProductoIngrediente pi : receta) {
                Inventario ing = pi.getInventario();
                BigDecimal cantidadNecesaria = pi.getCantidadUsada().multiply(BigDecimal.valueOf(item.getCantidad()));
                if (ing.getStockActual().compareTo(cantidadNecesaria) < 0) {
                    throw new BusinessException("Stock insuficiente de: " + ing.getNombreIngrediente());
                }
                BigDecimal stockAnterior = ing.getStockActual();
                ing.setStockActual(stockAnterior.subtract(cantidadNecesaria));
                inventarioRepository.save(ing);

                MovimientoInventario mov = MovimientoInventario.builder()
                        .inventario(ing)
                        .tipo(MovimientoInventario.TipoMovimiento.SALIDA)
                        .cantidad(cantidadNecesaria)
                        .stockAnterior(stockAnterior)
                        .stockNuevo(ing.getStockActual())
                        .referencia(pedido.getNumeroPedido())
                        .motivo("Venta de producto")
                        .build();
                movimientoInventarioRepository.save(mov);
            }
        }
    }

    private void devolverInventario(Pedido pedido) {
        List<DetallePedido> items = detallePedidoRepository.findByPedidoId(pedido.getId());
        for (DetallePedido item : items) {
            if (!item.getProducto().getEsPreparado()) continue;
            List<ProductoIngrediente> receta = productoIngredienteRepository.findByProductoId(item.getProducto().getId());
            for (ProductoIngrediente pi : receta) {
                Inventario ing = pi.getInventario();
                BigDecimal cantidadDevolver = pi.getCantidadUsada().multiply(BigDecimal.valueOf(item.getCantidad()));
                BigDecimal stockAnterior = ing.getStockActual();
                ing.setStockActual(stockAnterior.add(cantidadDevolver));
                inventarioRepository.save(ing);

                MovimientoInventario mov = MovimientoInventario.builder()
                        .inventario(ing)
                        .tipo(MovimientoInventario.TipoMovimiento.ENTRADA)
                        .cantidad(cantidadDevolver)
                        .stockAnterior(stockAnterior)
                        .stockNuevo(ing.getStockActual())
                        .referencia("CANCEL-" + pedido.getNumeroPedido())
                        .motivo("Cancelación de pedido")
                        .build();
                movimientoInventarioRepository.save(mov);
            }
        }
    }

    public List<PedidoDTO.Response> listarActivos() {
        return pedidoRepository.findPedidosActivos().stream()
                .map(p -> toResponse(p, detallePedidoRepository.findByPedidoId(p.getId())))
                .collect(Collectors.toList());
    }

    public List<PedidoDTO.Response> listarTodos() {
        return pedidoRepository.findAll(org.springframework.data.domain.Sort.by("fechaCreacion").ascending()).stream()
                .map(p -> toResponse(p, detallePedidoRepository.findByPedidoId(p.getId())))
                .collect(Collectors.toList());
    }

    public List<PedidoDTO.Response> listarParaCocina() {
        return pedidoRepository.findPedidosParaCocina().stream()
                .map(p -> toResponse(p, detallePedidoRepository.findByPedidoId(p.getId())))
                .collect(Collectors.toList());
    }

    public PedidoDTO.Response buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
        return toResponse(pedido, detallePedidoRepository.findByPedidoId(id));
    }

    private String generarNumeroPedido() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PED-" + timestamp;
    }

    private PedidoDTO.Response toResponse(Pedido p, List<DetallePedido> detalles) {
        PedidoDTO.Response r = new PedidoDTO.Response();
        r.setId(p.getId());
        r.setNumeroPedido(p.getNumeroPedido());
        r.setMesa(p.getMesa() != null ? "Mesa " + p.getMesa().getNumero() : null);
        r.setMesero(p.getUsuario().getNombre());
        r.setTipoPedido(p.getTipoPedido().name());
        r.setEstado(p.getEstado().name());
        r.setSubtotal(p.getSubtotal());
        r.setTotal(p.getTotal());
        r.setFechaCreacion(p.getFechaCreacion() != null ? p.getFechaCreacion().toString() : null);
        r.setNotasEspeciales(p.getNotasEspeciales());
        r.setItems(detalles.stream().map(d -> {
            PedidoDTO.DetalleResponse dr = new PedidoDTO.DetalleResponse();
            dr.setId(d.getId());
            dr.setProducto(d.getProducto().getNombre());
            dr.setCategoria(d.getProducto().getCategoria() != null ? d.getProducto().getCategoria().getNombre() : null);
            dr.setCantidad(d.getCantidad());
            dr.setPrecioUnitario(d.getPrecioUnitario());
            dr.setSubtotal(d.getSubtotal());
            dr.setNotas(d.getNotas());
            dr.setEstado(d.getEstado().name());
            return dr;
        }).collect(Collectors.toList()));
        return r;
    }
}
