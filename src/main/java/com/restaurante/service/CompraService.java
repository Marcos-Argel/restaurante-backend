package com.restaurante.service;

import com.restaurante.entity.*;
import com.restaurante.exception.*;
import com.restaurante.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    public Compra buscarPorId(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra", id));
    }

    public List<DetalleCompra> detalleCompra(Long compraId) {
        return detalleCompraRepository.findByCompraId(compraId);
    }

    @Transactional
    public Compra registrarCompra(RegistrarCompraRequest request, String emailUsuario) {
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", request.getProveedorId()));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Compra compra = Compra.builder()
                .proveedor(proveedor)
                .numeroFactura(request.getNumeroFactura())
                .subtotal(request.getSubtotal())
                .impuesto(request.getImpuesto() != null ? request.getImpuesto() : BigDecimal.ZERO)
                .total(request.getTotal())
                .metodoPago(Compra.MetodoPagoCompra.valueOf(request.getMetodoPago()))
                .estado(Compra.EstadoCompra.RECIBIDA)
                .usuario(usuario)
                .notas(request.getNotas())
                .build();

        compra = compraRepository.save(compra);

        for (ItemCompra item : request.getItems()) {
            Inventario inv = inventarioRepository.findById(item.getInventarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventario", item.getInventarioId()));

            BigDecimal subtotal = item.getPrecioUnitario().multiply(item.getCantidad());

            DetalleCompra detalle = DetalleCompra.builder()
                    .compra(compra)
                    .inventario(inv)
                    .cantidad(item.getCantidad())
                    .precioUnitario(item.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build();
            detalleCompraRepository.save(detalle);

            // Actualizar inventario
            BigDecimal stockAnterior = inv.getStockActual();
            inv.setStockActual(stockAnterior.add(item.getCantidad()));
            inv.setCostoUnitario(item.getPrecioUnitario());
            inv.setFechaActualizacion(LocalDateTime.now());
            inventarioRepository.save(inv);

            // Registrar movimiento
            MovimientoInventario mov = MovimientoInventario.builder()
                    .inventario(inv)
                    .tipo(MovimientoInventario.TipoMovimiento.ENTRADA)
                    .cantidad(item.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockNuevo(inv.getStockActual())
                    .referencia("COMPRA-" + compra.getId())
                    .motivo("Compra a proveedor")
                    .usuario(usuario)
                    .build();
            movimientoRepository.save(mov);
        }

        return compra;
    }

    @Data
    public static class RegistrarCompraRequest {
        private Long proveedorId;
        private String numeroFactura;
        private BigDecimal subtotal;
        private BigDecimal impuesto;
        private BigDecimal total;
        private String metodoPago;
        private String notas;
        private List<ItemCompra> items;
    }

    @Data
    public static class ItemCompra {
        private Long inventarioId;
        private BigDecimal cantidad;
        private BigDecimal precioUnitario;
    }
}
