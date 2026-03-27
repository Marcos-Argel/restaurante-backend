package com.restaurante.service;

import com.restaurante.entity.*;
import com.restaurante.exception.*;
import com.restaurante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoCajaService {

    private final TurnoCajaRepository turnoCajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FacturaRepository facturaRepository;

    @Transactional
    public TurnoCaja abrirTurno(Integer usuarioId, BigDecimal montoInicial) {
        turnoCajaRepository.findByUsuarioIdAndEstado(usuarioId, TurnoCaja.EstadoTurno.ABIERTO)
                .ifPresent(t -> { throw new BusinessException("El usuario ya tiene un turno abierto"); });

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        TurnoCaja turno = TurnoCaja.builder()
                .usuario(usuario)
                .montoInicial(montoInicial)
                .estado(TurnoCaja.EstadoTurno.ABIERTO)
                .build();

        return turnoCajaRepository.save(turno);
    }

    @Transactional
    public TurnoCaja cerrarTurno(Long turnoId, BigDecimal montoFinal) {
        TurnoCaja turno = turnoCajaRepository.findById(turnoId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno", turnoId));

        if (turno.getEstado() == TurnoCaja.EstadoTurno.CERRADO) {
            throw new BusinessException("El turno ya está cerrado");
        }

        // Calcular totales del turno
        List<Factura> facturas = facturaRepository.findByTurnoId(turnoId);
        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal totalEfectivo = BigDecimal.ZERO;
        BigDecimal totalTarjeta = BigDecimal.ZERO;
        BigDecimal totalTransferencia = BigDecimal.ZERO;

        for (Factura f : facturas) {
            if (f.getEstado() == Factura.EstadoFactura.EMITIDA) {
                totalVentas = totalVentas.add(f.getMontoTotal());
                switch (f.getMetodoPago()) {
                    case EFECTIVO -> totalEfectivo = totalEfectivo.add(f.getMontoTotal());
                    case TARJETA_DEBITO, TARJETA_CREDITO -> totalTarjeta = totalTarjeta.add(f.getMontoTotal());
                    case TRANSFERENCIA -> totalTransferencia = totalTransferencia.add(f.getMontoTotal());
                    default -> {}
                }
            }
        }

        BigDecimal esperado = turno.getMontoInicial().add(totalEfectivo);
        BigDecimal diferencia = montoFinal.subtract(esperado);

        turno.setFechaCierre(LocalDateTime.now());
        turno.setMontoFinal(montoFinal);
        turno.setTotalVentas(totalVentas);
        turno.setTotalEfectivo(totalEfectivo);
        turno.setTotalTarjeta(totalTarjeta);
        turno.setTotalTransferencia(totalTransferencia);
        turno.setDiferencia(diferencia);
        turno.setEstado(TurnoCaja.EstadoTurno.CERRADO);

        return turnoCajaRepository.save(turno);
    }

    public TurnoCaja buscarTurnoAbierto(Integer usuarioId) {
        return turnoCajaRepository.findByUsuarioIdAndEstado(usuarioId, TurnoCaja.EstadoTurno.ABIERTO)
                .orElseThrow(() -> new ResourceNotFoundException("No hay turno abierto para este usuario"));
    }

    public List<TurnoCaja> listarTodos() {
        return turnoCajaRepository.findAll();
    }
}
