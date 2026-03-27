package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.TurnoCaja;
import com.restaurante.service.TurnoCajaService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoCajaController {

    private final TurnoCajaService turnoCajaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TurnoCaja>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(turnoCajaService.listarTodos()));
    }

    @GetMapping("/activo/{usuarioId}")
    public ResponseEntity<ApiResponse<TurnoCaja>> turnoActivo(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(ApiResponse.ok(turnoCajaService.buscarTurnoAbierto(usuarioId)));
    }

    @PostMapping("/abrir")
    public ResponseEntity<ApiResponse<TurnoCaja>> abrir(@RequestBody AbrirTurnoRequest request) {
        TurnoCaja turno = turnoCajaService.abrirTurno(request.getUsuarioId(), request.getMontoInicial());
        return ResponseEntity.ok(ApiResponse.ok("Turno abierto", turno));
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<ApiResponse<TurnoCaja>> cerrar(
            @PathVariable Long id, @RequestBody CerrarTurnoRequest request) {
        TurnoCaja turno = turnoCajaService.cerrarTurno(id, request.getMontoFinal());
        return ResponseEntity.ok(ApiResponse.ok("Turno cerrado", turno));
    }

    @Data
    public static class AbrirTurnoRequest {
        private Integer usuarioId;
        private BigDecimal montoInicial;
    }

    @Data
    public static class CerrarTurnoRequest {
        private BigDecimal montoFinal;
    }
}
