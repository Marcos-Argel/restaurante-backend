package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.dto.FacturaDTO;
import com.restaurante.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FacturaDTO.Response>>> listarTodas() {
        return ResponseEntity.ok(ApiResponse.ok(facturaService.listarTodas()));
    }

    @GetMapping("/hoy")
    public ResponseEntity<ApiResponse<List<FacturaDTO.Response>>> listarHoy() {
        return ResponseEntity.ok(ApiResponse.ok(facturaService.listarHoy()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacturaDTO.Response>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(facturaService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FacturaDTO.Response>> crear(
            @Valid @RequestBody FacturaDTO.CrearFacturaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        FacturaDTO.Response response = facturaService.crearFactura(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Factura generada", response));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<ApiResponse<FacturaDTO.Response>> anular(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Factura anulada",
                facturaService.anularFactura(id, userDetails.getUsername())));
    }
}
