package com.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", unique = true, length = 20)
    private String numeroPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "cliente_nombre", length = 100)
    private String clienteNombre;

    @Column(name = "cliente_telefono", length = 20)
    private String clienteTelefono;

    @Column(name = "cliente_direccion", columnDefinition = "TEXT")
    private String clienteDireccion;

    // --- CORRECCIONES CON @Builder.Default ---

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pedido")
    private TipoPedido tipoPedido = TipoPedido.MESA;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // --- FIN DE CORRECCIONES ---

    @Column(name = "fecha_preparacion")
    private LocalDateTime fechaPreparacion;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "notas_especiales", columnDefinition = "TEXT")
    private String notasEspeciales;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modificado_por")
    private Usuario modificadoPor;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.EAGER)
    private List<DetallePedido> detalles;

    public List<DetallePedido> getItems() {
        return detalles;
    }

    public enum TipoPedido {
        MESA, DOMICILIO, PARA_LLEVAR
    }

    public enum EstadoPedido {
        PENDIENTE, EN_PREPARACION, LISTO, SERVIDO, PAGADO, CANCELADO
    }
}