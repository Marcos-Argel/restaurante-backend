package com.restaurante.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mesa_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservas", "pedidos"})
    private Mesa mesa;

    @Column(name = "cliente_nombre", nullable = false, length = 100)
    private String clienteNombre;

    @Column(name = "cliente_telefono", nullable = false, length = 20)
    private String clienteTelefono;

    @Column(name = "cliente_email", length = 100)
    private String clienteEmail;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "cantidad_personas", nullable = false)
    private Integer cantidadPersonas;

    // --- CORRECCIÓN: Garantiza que la reserva nazca con estado CONFIRMADA ---
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "total_estimado", precision = 10, scale = 2)
    private java.math.BigDecimal totalEstimado;

    @Column(name = "anticipo", precision = 10, scale = 2)
    private java.math.BigDecimal anticipo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "reservas"})
    private Usuario usuario;

    // --- CORRECCIÓN: Asegura el timestamp de registro ---
    @Builder.Default
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public enum EstadoReserva {
        CONFIRMADA, CANCELADA, COMPLETADA, NO_ASISTIO
    }
}