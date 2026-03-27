package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.dto.AuthDTO;
import com.restaurante.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDTO.LoginResponse>> login(
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        AuthDTO.LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", response));
    }
}
