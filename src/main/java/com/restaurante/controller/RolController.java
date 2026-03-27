package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.Rol;
import com.restaurante.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolRepository rolRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Rol>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(rolRepository.findAll()));
    }
}
