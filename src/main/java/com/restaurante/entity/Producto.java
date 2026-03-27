package com.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(name = "tiempo_preparacion")
    private Integer tiempoPreparacion;

    @Enumerated(EnumType.STRING)
    private EstadoProducto estado = EstadoProducto.ACTIVO;

    @Column(name = "es_preparado")
    private Boolean esPreparado = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    public enum EstadoProducto {
        ACTIVO, INACTIVO, AGOTADO
    }
}
