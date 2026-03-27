package com.restaurante.repository;
import com.restaurante.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
    List<Pedido> findByEstadoNotIn(List<Pedido.EstadoPedido> estados);
    List<Pedido> findByMesaIdAndEstadoNotIn(Integer mesaId, List<Pedido.EstadoPedido> estados);

    @Query("SELECT p FROM Pedido p WHERE p.estado NOT IN ('PAGADO','CANCELADO') ORDER BY p.fechaCreacion ASC")
    List<Pedido> findPedidosActivos();

    @Query("SELECT p FROM Pedido p WHERE DATE(p.fechaCreacion) = CURRENT_DATE AND p.estado IN ('PAGADO','SERVIDO')")
    List<Pedido> findPedidosHoy();

    @Query("SELECT p FROM Pedido p WHERE p.fechaCreacion BETWEEN :inicio AND :fin")
    List<Pedido> findByFechaCreacionBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT p FROM Pedido p WHERE p.estado IN ('PENDIENTE','EN_PREPARACION') ORDER BY p.fechaCreacion ASC")
    List<Pedido> findPedidosParaCocina();
}
