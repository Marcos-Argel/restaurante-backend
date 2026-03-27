package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.Categoria;
import com.restaurante.exception.ResourceNotFoundException;
import com.restaurante.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Categoria>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(categoriaRepository.findByActivoTrueOrderByOrdenMenuAsc()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Categoria>> buscar(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Categoria>> crear(@RequestBody Categoria categoria) {
        categoria.setActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Categoría creada", categoriaRepository.save(categoria)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Categoria>> actualizar(
            @PathVariable Integer id, @RequestBody Categoria datos) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
        c.setNombre(datos.getNombre());
        c.setDescripcion(datos.getDescripcion());
        c.setOrdenMenu(datos.getOrdenMenu());
        return ResponseEntity.ok(ApiResponse.ok("Categoría actualizada", categoriaRepository.save(c)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
        c.setActivo(false);
        categoriaRepository.save(c);
        return ResponseEntity.ok(ApiResponse.ok("Categoría desactivada", null));
    }
}
