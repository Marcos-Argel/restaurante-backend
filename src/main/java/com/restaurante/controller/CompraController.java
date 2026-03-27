package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.Compra;
import com.restaurante.entity.DetalleCompra;
import com.restaurante.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Compra>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(compraService.listarTodas()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Compra>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(compraService.buscarPorId(id)));
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<ApiResponse<List<DetalleCompra>>> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(compraService.detalleCompra(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Compra>> registrar(
            @RequestBody CompraService.RegistrarCompraRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Compra compra = compraService.registrarCompra(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Compra registrada", compra));
    }
}
