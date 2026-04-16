package com.restaurante.config;

import com.restaurante.security.JwtAuthFilter;
import com.restaurante.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // IMPORTANTE: Permitir el origen de tu frontend en Render si es posible,
        // o mantener "*" con precaución.
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers explícitos para evitar bloqueos del navegador
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS DEBE IR PRIMERO EN LA CADENA
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 2. PERMITIR OPTIONS EXPLÍCITAMENTE ANTES QUE NADA
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 3. RUTAS PÚBLICAS
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/menu").permitAll()
                        //Permitir crear usuarios sin estar logueados
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        // 4. SEGURIDAD POR ROLES
                        .requestMatchers("/api/usuarios/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/roles/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/reportes/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/inventario/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/compras/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/proveedores/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/cocina/**").hasAnyRole("ADMIN", "GERENTE", "COCINERO")
                        .requestMatchers("/api/turnos/**").hasAnyRole("ADMIN", "GERENTE", "CAJERO")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                // 5. JWT FILTER DESPUÉS DE CORS PARA NO INTERFERIR EN PREFLIGHT
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}