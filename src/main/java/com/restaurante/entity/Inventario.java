package com.restaurante.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_ingrediente", nullable = false, length = 100)
    private String nombreIngrediente;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedida unidadMedida;

    // --- CORRECCIONES CON @Builder.Default ---

    @Builder.Default
    @Column(name = "stock_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockActual = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "stock_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockMinimo = new BigDecimal("5");

    @Builder.Default
    @Column(name = "stock_alerta", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockAlerta = new BigDecimal("10");

    @Builder.Default
    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    // --- FIN DE CORRECCIONES ---

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "inventarios"})
    private Proveedor proveedor;

    @Column(length = 100)
    private String ubicacion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    public enum UnidadMedida {
        KG, G, L, ML, UNIDAD, PORCION
    }
}