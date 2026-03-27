package com.restaurante.repository;
import com.restaurante.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByNumeroFactura(String numeroFactura);
    Optional<Factura> findByPedidoId(Long pedidoId);
    @Query("SELECT f FROM Factura f WHERE DATE(f.fecha) = CURRENT_DATE AND f.estado = 'EMITIDA'")
    List<Factura> findFacturasHoy();
    @Query("SELECT f FROM Factura f WHERE f.fecha BETWEEN :inicio AND :fin AND f.estado = 'EMITIDA'")
    List<Factura> findByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
    List<Factura> findByTurnoId(Long turnoId);
    List<Factura> findAllByOrderByFechaDesc();
}
