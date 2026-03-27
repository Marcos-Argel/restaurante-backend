package com.restaurante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthDTO {

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String tipo = "Bearer";
        private Integer id;
        private String nombre;
        private String email;
        private String rol;

        public LoginResponse(String token, Integer id, String nombre, String email, String rol) {
            this.token = token;
            this.id = id;
            this.nombre = nombre;
            this.email = email;
            this.rol = rol;
        }
    }
}
