package com.restaurante.service;

import com.restaurante.dto.UsuarioDTO;
import com.restaurante.entity.Rol;
import com.restaurante.entity.Usuario;
import com.restaurante.exception.*;
import com.restaurante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioDTO.Response> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<UsuarioDTO.Response> listarActivos() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UsuarioDTO.Response buscarPorId(Integer id) {
        return toResponse(usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id)));
    }

    @Transactional
    public UsuarioDTO.Response crear(UsuarioDTO.Request request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe un usuario con el email: " + request.getEmail());
        }
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol", request.getRolId()));

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .rol(rol)
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioDTO.Response actualizar(Integer id, UsuarioDTO.Request request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol", request.getRolId()));

        usuario.setNombre(request.getNombre());
        usuario.setTelefono(request.getTelefono());
        usuario.setRol(rol);
        if (request.getActivo() != null) usuario.setActivo(request.getActivo());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void cambiarEstado(Integer id, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    private UsuarioDTO.Response toResponse(Usuario u) {
        UsuarioDTO.Response r = new UsuarioDTO.Response();
        r.setId(u.getId());
        r.setNombre(u.getNombre());
        r.setEmail(u.getEmail());
        r.setTelefono(u.getTelefono());
        r.setRol(u.getRol().getNombre());
        r.setActivo(u.getActivo());
        r.setFechaCreacion(u.getFechaCreacion() != null ? u.getFechaCreacion().toString() : null);
        return r;
    }
}
