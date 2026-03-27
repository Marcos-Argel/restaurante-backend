package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.Inventario;
import com.restaurante.entity.MovimientoInventario;
import com.restaurante.service.InventarioService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Inventario>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(inventarioService.listarTodos()));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<ApiResponse<List<Inventario>>> stockBajo() {
        return ResponseEntity.ok(ApiResponse.ok(inventarioService.listarStockBajo()));
    }

    @GetMapping("/stock-critico")
    public ResponseEntity<ApiResponse<List<Inventario>>> stockCritico() {
        return ResponseEntity.ok(ApiResponse.ok(inventarioService.listarStockCritico()));
    }

    @GetMapping("/proximos-vencer")
    public ResponseEntity<ApiResponse<List<Inventario>>> proximosAVencer() {
        return ResponseEntity.ok(ApiResponse.ok(inventarioService.listarProximosAVencer()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Inventario>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventarioService.buscarPorId(id)));
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<ApiResponse<List<MovimientoInventario>>> movimientos(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventarioService.historialMovimientos(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Inventario>> crear(@RequestBody Inventario inventario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Ingrediente creado", inventarioService.crear(inventario)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Inventario>> actualizar(
            @PathVariable Long id, @RequestBody Inventario datos) {
        return ResponseEntity.ok(ApiResponse.ok("Ingrediente actualizado", inventarioService.actualizar(id, datos)));
    }

    @PostMapping("/{id}/ajuste")
    public ResponseEntity<ApiResponse<Inventario>> ajustarStock(
            @PathVariable Long id,
            @RequestBody AjusteStockRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Inventario inv = inventarioService.ajustarStock(
                id, request.getCantidad(),
                MovimientoInventario.TipoMovimiento.valueOf(request.getTipo()),
                request.getMotivo(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Stock ajustado", inv));
    }

    @Data
    public static class AjusteStockRequest {
        private BigDecimal cantidad;
        private String tipo;
        private String motivo;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        inventarioService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Ingrediente eliminado", null));
    }

}