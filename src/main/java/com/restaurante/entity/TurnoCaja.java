package com.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "turnos_caja")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura = LocalDateTime.now();

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "monto_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "monto_final", precision = 10, scale = 2)
    private BigDecimal montoFinal;

    @Column(name = "total_ventas", precision = 10, scale = 2)
    private BigDecimal totalVentas = BigDecimal.ZERO;

    @Column(name = "total_efectivo", precision = 10, scale = 2)
    private BigDecimal totalEfectivo = BigDecimal.ZERO;

    @Column(name = "total_tarjeta", precision = 10, scale = 2)
    private BigDecimal totalTarjeta = BigDecimal.ZERO;

    @Column(name = "total_transferencia", precision = 10, scale = 2)
    private BigDecimal totalTransferencia = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal diferencia = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private EstadoTurno estado = EstadoTurno.ABIERTO;

    @Column(columnDefinition = "TEXT")
    private String notas;

    public enum EstadoTurno {
        ABIERTO, CERRADO
    }
}
