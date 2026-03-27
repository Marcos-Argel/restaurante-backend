package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.Mesa;
import com.restaurante.exception.ResourceNotFoundException;
import com.restaurante.repository.MesaRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final MesaRepository mesaRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Mesa>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(mesaRepository.findAllByOrderByNumeroAsc()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Mesa>> buscar(@PathVariable Integer id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", id));
        return ResponseEntity.ok(ApiResponse.ok(mesa));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Mesa>> crear(@RequestBody MesaRequest request) {
        Mesa mesa = Mesa.builder()
                .numero(request.getNumero())
                .capacidad(request.getCapacidad())
                .zona(request.getZona())
                .descripcion(request.getDescripcion())
                .estado(Mesa.EstadoMesa.LIBRE)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Mesa creada", mesaRepository.save(mesa)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Mesa>> actualizar(
            @PathVariable Integer id, @RequestBody MesaRequest request) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", id));
        mesa.setNumero(request.getNumero());
        mesa.setCapacidad(request.getCapacidad());
        mesa.setZona(request.getZona());
        mesa.setDescripcion(request.getDescripcion());
        return ResponseEntity.ok(ApiResponse.ok("Mesa actualizada", mesaRepository.save(mesa)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Mesa>> cambiarEstado(
            @PathVariable Integer id, @RequestParam String estado) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", id));
        mesa.setEstado(Mesa.EstadoMesa.valueOf(estado));
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", mesaRepository.save(mesa)));
    }

    @Data
    public static class MesaRequest {
        private Integer numero;
        private Integer capacidad;
        private String zona;
        private String descripcion;
    }
}
