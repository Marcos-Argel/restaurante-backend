package com.restaurante.service;

import com.restaurante.dto.AuthDTO;
import com.restaurante.entity.Usuario;
import com.restaurante.repository.UsuarioRepository;
import com.restaurante.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder; // IMPORTANTE
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder; // Inyección para BCrypt

    @Transactional
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        // Spring Security comparará el password plano del request con el hash de la DB
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtUtils.generateToken(usuario.getEmail(), usuario.getRol().getNombre());

        return new AuthDTO.LoginResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().getNombre()
        );
    }

    @Transactional
    public void register(Usuario usuario) {
        // 1. Verificación técnica de duplicados
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado en el sistema.");
        }

        // 2. ENCRIPTACIÓN: Pasamos de "admin123" a "$2a$10$..."
        // Sin esto, el login que tienes arriba fallará siempre (401).
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // 3. Valores por defecto (aunque los tengas en la entidad, aquí aseguramos el estado)
        if (usuario.getActivo() == null) usuario.setActivo(true);
        if (usuario.getFechaCreacion() == null) usuario.setFechaCreacion(LocalDateTime.now());

        // 4. Persistencia en Aiven
        usuarioRepository.save(usuario);
    }
}