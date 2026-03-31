package com.restaurante.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "producto_ingrediente",
        uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "inventario_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoIngrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "ingredientes", "categoria"})
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inventario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "proveedor"})
    private Inventario inventario;

    @Column(name = "cantidad_usada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadUsada;

    @Column(columnDefinition = "TEXT")
    private String notas;
}