package com.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mesas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(length = 50)
    private String zona;

    @Column(length = 100)
    private String descripcion;

    // --- CORRECCIÓN: Garantiza que la mesa no nazca con estado NULL ---
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private EstadoMesa estado = EstadoMesa.LIBRE;

    // --- CORRECCIÓN: Asegura la marca de tiempo al usar el Builder ---
    @Builder.Default
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public enum EstadoMesa {
        LIBRE, OCUPADA, RESERVADA, FUERA_SERVICIO
    }
}