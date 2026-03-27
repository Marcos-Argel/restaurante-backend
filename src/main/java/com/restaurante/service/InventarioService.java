package com.restaurante.service;

import com.restaurante.entity.*;
import com.restaurante.exception.*;
import com.restaurante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Inventario> listarTodos() {
        return inventarioRepository.findAll();
    }

    public List<Inventario> listarStockBajo() {
        return inventarioRepository.findStockBajo();
    }

    public List<Inventario> listarStockCritico() {
        return inventarioRepository.findStockCritico();
    }

    public List<Inventario> listarProximosAVencer() {
        return inventarioRepository.findProximosAVencer(LocalDate.now().plusDays(7));
    }

    public Inventario buscarPorId(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", id));
    }

    @Transactional
    public Inventario crear(Inventario inventario) {
        inventario.setFechaActualizacion(LocalDateTime.now());
        return inventarioRepository.save(inventario);
    }

    @Transactional
    public Inventario actualizar(Long id, Inventario datos) {
        Inventario inv = buscarPorId(id);
        inv.setNombreIngrediente(datos.getNombreIngrediente());
        inv.setDescripcion(datos.getDescripcion());
        inv.setUnidadMedida(datos.getUnidadMedida());
        inv.setStockMinimo(datos.getStockMinimo());
        inv.setStockAlerta(datos.getStockAlerta());
        inv.setCostoUnitario(datos.getCostoUnitario());
        inv.setUbicacion(datos.getUbicacion());
        inv.setFechaVencimiento(datos.getFechaVencimiento());
        inv.setFechaActualizacion(LocalDateTime.now());
        return inventarioRepository.save(inv);
    }

    @Transactional
    public Inventario ajustarStock(Long inventarioId, BigDecimal cantidad,
                                    MovimientoInventario.TipoMovimiento tipo,
                                    String motivo, String emailUsuario) {
        Inventario inv = buscarPorId(inventarioId);
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElse(null);

        BigDecimal stockAnterior = inv.getStockActual();
        BigDecimal stockNuevo;

        if (tipo == MovimientoInventario.TipoMovimiento.ENTRADA) {
            stockNuevo = stockAnterior.add(cantidad);
        } else if (tipo == MovimientoInventario.TipoMovimiento.SALIDA || tipo == MovimientoInventario.TipoMovimiento.MERMA) {
            if (stockAnterior.compareTo(cantidad) < 0) {
                throw new BusinessException("Stock insuficiente");
            }
            stockNuevo = stockAnterior.subtract(cantidad);
        } else { // AJUSTE
            stockNuevo = cantidad;
        }

        inv.setStockActual(stockNuevo);
        inv.setFechaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inv);

        MovimientoInventario mov = MovimientoInventario.builder()
                .inventario(inv)
                .tipo(tipo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .motivo(motivo)
                .usuario(usuario)
                .build();
        movimientoRepository.save(mov);

        return inv;
    }

    public List<MovimientoInventario> historialMovimientos(Long inventarioId) {
        return movimientoRepository.findByInventarioIdOrderByFechaDesc(inventarioId);
    }

    public void eliminar(Long id) {
        Inventario inv = inventarioRepository.findById(id)
                .orElseThrow(() -> new com.restaurante.exception.ResourceNotFoundException("Ingrediente", id));
        inventarioRepository.delete(inv);
    }

}