package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.dto.UsuarioDTO;
import com.restaurante.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioDTO.Response>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.listarActivos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioDTO.Response>> buscar(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioDTO.Response>> crear(
            @Valid @RequestBody UsuarioDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario creado exitosamente", usuarioService.crear(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioDTO.Response>> actualizar(
            @PathVariable Integer id, @Valid @RequestBody UsuarioDTO.Request request) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", usuarioService.actualizar(id, request)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer id, @RequestParam Boolean activo) {
        usuarioService.cambiarEstado(id, activo);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", null));
    }
}
