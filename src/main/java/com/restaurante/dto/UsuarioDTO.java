package com.restaurante.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

public class UsuarioDTO {

    @Data
    public static class Request {
        @NotBlank
        private String nombre;
        @NotBlank @Email
        private String email;
        private String password;
        private String telefono;
        @NotNull
        private Integer rolId;
        private Boolean activo = true;
    }

    @Data
    public static class Response {
        private Integer id;
        private String nombre;
        private String email;
        private String telefono;
        private String rol;
        private Boolean activo;
        private String fechaCreacion;
    }
}
