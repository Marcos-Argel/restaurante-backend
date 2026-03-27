package com.restaurante.repository;
import com.restaurante.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByInventarioIdOrderByFechaDesc(Long inventarioId);
    List<MovimientoInventario> findByTipo(MovimientoInventario.TipoMovimiento tipo);
}
