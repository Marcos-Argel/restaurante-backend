package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.Inventario;
import com.restaurante.entity.Producto;
import com.restaurante.entity.ProductoIngrediente;
import com.restaurante.exception.ResourceNotFoundException;
import com.restaurante.repository.InventarioRepository;
import com.restaurante.repository.ProductoIngredienteRepository;
import com.restaurante.repository.ProductoRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final ProductoIngredienteRepository recetaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<List<ProductoIngrediente>>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(ApiResponse.ok(recetaRepository.findByProductoId(productoId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoIngrediente>> agregar(@RequestBody RecetaRequest request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", request.getProductoId()));
        Inventario inventario = inventarioRepository.findById(request.getInventarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", request.getInventarioId()));

        ProductoIngrediente pi = ProductoIngrediente.builder()
                .producto(producto)
                .inventario(inventario)
                .cantidadUsada(request.getCantidadUsada())
                .notas(request.getNotas())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Ingrediente agregado a receta", recetaRepository.save(pi)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoIngrediente>> actualizar(
            @PathVariable Long id, @RequestBody RecetaRequest request) {
        ProductoIngrediente pi = recetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", id));
        pi.setCantidadUsada(request.getCantidadUsada());
        pi.setNotas(request.getNotas());
        return ResponseEntity.ok(ApiResponse.ok("Receta actualizada", recetaRepository.save(pi)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        recetaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Receta", id));
        recetaRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Ingrediente eliminado de receta", null));
    }

    @Data
    public static class RecetaRequest {
        private Long productoId;
        private Long inventarioId;
        private BigDecimal cantidadUsada;
        private String notas;
    }
}
