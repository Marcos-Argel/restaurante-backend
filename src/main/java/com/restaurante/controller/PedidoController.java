package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.dto.PedidoDTO;
import com.restaurante.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoDTO.Response>>> listarActivos() {
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.listarActivos()));
    }

    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<PedidoDTO.Response>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.listarTodos()));
    }

    @GetMapping("/cocina")
    public ResponseEntity<ApiResponse<List<PedidoDTO.Response>>> listarCocina() {
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.listarParaCocina()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoDTO.Response>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PedidoDTO.Response>> crear(
            @Valid @RequestBody PedidoDTO.CrearPedidoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PedidoDTO.Response response = pedidoService.crearPedido(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pedido creado", response));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<PedidoDTO.Response>> cambiarEstado(
            @PathVariable Long id,
            @RequestBody PedidoDTO.CambiarEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PedidoDTO.Response response = pedidoService.cambiarEstado(id, request.getEstado(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", response));
    }
}
