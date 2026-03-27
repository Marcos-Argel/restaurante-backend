package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.Mesa;
import com.restaurante.entity.Reserva;
import com.restaurante.entity.Usuario;
import com.restaurante.exception.ResourceNotFoundException;
import com.restaurante.repository.MesaRepository;
import com.restaurante.repository.ReservaRepository;
import com.restaurante.repository.UsuarioRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaRepository reservaRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Reserva>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(reservaRepository.findAll()));
    }

    @GetMapping("/confirmadas")
    public ResponseEntity<ApiResponse<List<Reserva>>> confirmadas() {
        return ResponseEntity.ok(ApiResponse.ok(reservaRepository.findByEstado(Reserva.EstadoReserva.CONFIRMADA)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Reserva>> crear(
            @RequestBody ReservaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Mesa mesa = mesaRepository.findById(request.getMesaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", request.getMesaId()));
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElse(null);

        Reserva reserva = Reserva.builder()
                .mesa(mesa)
                .clienteNombre(request.getClienteNombre())
                .clienteTelefono(request.getClienteTelefono())
                .clienteEmail(request.getClienteEmail())
                .fechaReserva(request.getFechaReserva())
                .cantidadPersonas(request.getCantidadPersonas())
                .notas(request.getNotas())
                .totalEstimado(request.getTotalEstimado())
                .anticipo(request.getAnticipo())
                .estado(Reserva.EstadoReserva.CONFIRMADA)
                .usuario(usuario)
                .build();

        mesa.setEstado(Mesa.EstadoMesa.RESERVADA);
        mesaRepository.save(mesa);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Reserva creada", reservaRepository.save(reserva)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Reserva>> cambiarEstado(
            @PathVariable Long id, @RequestParam String estado) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        Reserva.EstadoReserva estadoNuevo = Reserva.EstadoReserva.valueOf(estado);
        reserva.setEstado(estadoNuevo);

        if (estadoNuevo == Reserva.EstadoReserva.CANCELADA || estadoNuevo == Reserva.EstadoReserva.COMPLETADA) {
            Mesa mesa = reserva.getMesa();
            mesa.setEstado(Mesa.EstadoMesa.LIBRE);
            mesaRepository.save(mesa);
        }

        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", reservaRepository.save(reserva)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        if (reserva.getMesa() != null && reserva.getEstado() == Reserva.EstadoReserva.CONFIRMADA) {
            Mesa mesa = reserva.getMesa();
            mesa.setEstado(Mesa.EstadoMesa.LIBRE);
            mesaRepository.save(mesa);
        }
        reservaRepository.delete(reserva);
        return ResponseEntity.ok(ApiResponse.ok("Reserva eliminada", null));
    }

    @Data
    public static class ReservaRequest {
        private Integer mesaId;
        private String clienteNombre;
        private String clienteTelefono;
        private String clienteEmail;
        private LocalDateTime fechaReserva;
        private Integer cantidadPersonas;
        private String notas;
        private java.math.BigDecimal totalEstimado;
        private java.math.BigDecimal anticipo;
    }
}
