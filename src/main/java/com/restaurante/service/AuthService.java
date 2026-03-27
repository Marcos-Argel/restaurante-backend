package com.restaurante.service;

import com.restaurante.dto.AuthDTO;
import com.restaurante.entity.Usuario;
import com.restaurante.repository.UsuarioRepository;
import com.restaurante.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtils jwtUtils;

    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtUtils.generateToken(usuario.getEmail(), usuario.getRol().getNombre());
        return new AuthDTO.LoginResponse(token, usuario.getId(), usuario.getNombre(),
                usuario.getEmail(), usuario.getRol().getNombre());
    }
}
