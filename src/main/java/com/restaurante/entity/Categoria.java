package com.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // --- CORRECCIONES CON @Builder.Default ---

    @Builder.Default
    @Column(name = "orden_menu")
    private Integer ordenMenu = 0;

    @Builder.Default
    private Boolean activo = true;

    // --- FIN DE CORRECCIONES ---
}