package com.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(name = "numero_factura", length = 50)
    private String numeroFactura;

    // --- CORRECCIONES CON @Builder.Default ---

    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal impuesto = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPagoCompra metodoPago = MetodoPagoCompra.EFECTIVO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private EstadoCompra estado = EstadoCompra.PENDIENTE;

    // --- FIN DE CORRECCIONES ---

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(columnDefinition = "TEXT")
    private String notas;

    public enum MetodoPagoCompra {
        EFECTIVO, TRANSFERENCIA, CREDITO
    }

    public enum EstadoCompra {
        PENDIENTE, RECIBIDA, CANCELADA
    }
}