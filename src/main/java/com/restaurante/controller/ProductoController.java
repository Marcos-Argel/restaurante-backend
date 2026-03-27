package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.Producto;
import com.restaurante.service.ProductoService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Producto>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(productoService.listarTodos()));
    }

    @GetMapping("/menu")
    public ResponseEntity<ApiResponse<List<Producto>>> listarActivos() {
        return ResponseEntity.ok(ApiResponse.ok(productoService.listarActivos()));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<List<Producto>>> listarPorCategoria(@PathVariable Integer categoriaId) {
        return ResponseEntity.ok(ApiResponse.ok(productoService.listarPorCategoria(categoriaId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Producto>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productoService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Producto>> crear(@RequestBody ProductoRequest request) {
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setTiempoPreparacion(request.getTiempoPreparacion());
        producto.setEsPreparado(request.getEsPreparado() != null ? request.getEsPreparado() : true);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Producto creado", productoService.crear(producto, request.getCategoriaId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Producto>> actualizar(
            @PathVariable Long id, @RequestBody ProductoRequest request) {
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setTiempoPreparacion(request.getTiempoPreparacion());
        producto.setEsPreparado(request.getEsPreparado() != null ? request.getEsPreparado() : true);
        producto.setImagenUrl(request.getImagenUrl());
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado",
                productoService.actualizar(id, producto, request.getCategoriaId())));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Producto>> cambiarEstado(
            @PathVariable Long id, @RequestParam String estado) {
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado",
                productoService.cambiarEstado(id, Producto.EstadoProducto.valueOf(estado))));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        try {
            productoService.eliminar(id);
            return ResponseEntity.ok(ApiResponse.ok("Producto eliminado", null));
        } catch (Exception e) {
            throw new com.restaurante.exception.BusinessException("No se puede eliminar el producto: " + e.getMessage());
        }
    }

    @Data
    public static class ProductoRequest {
        private String nombre;
        private String descripcion;
        private BigDecimal precio;
        private Integer categoriaId;
        private String imagenUrl;
        private Integer tiempoPreparacion;
        private Boolean esPreparado;
    }
}
