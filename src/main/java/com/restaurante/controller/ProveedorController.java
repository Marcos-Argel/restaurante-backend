package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.Proveedor;
import com.restaurante.exception.ResourceNotFoundException;
import com.restaurante.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Proveedor>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(proveedorRepository.findByActivoTrue()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Proveedor>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Proveedor>> crear(@RequestBody Proveedor proveedor) {
        proveedor.setActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Proveedor creado", proveedorRepository.save(proveedor)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Proveedor>> actualizar(
            @PathVariable Long id, @RequestBody Proveedor datos) {
        Proveedor p = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", id));
        p.setNombre(datos.getNombre());
        p.setRazonSocial(datos.getRazonSocial());
        p.setRucNit(datos.getRucNit());
        p.setTelefono(datos.getTelefono());
        p.setEmail(datos.getEmail());
        p.setDireccion(datos.getDireccion());
        p.setContactoNombre(datos.getContactoNombre());
        return ResponseEntity.ok(ApiResponse.ok("Proveedor actualizado", proveedorRepository.save(p)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Long id, @RequestParam Boolean activo) {
        Proveedor p = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", id));
        p.setActivo(activo);
        proveedorRepository.save(p);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", null));
    }
}
