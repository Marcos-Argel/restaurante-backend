package com.restaurante.repository;
import com.restaurante.entity.StockProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StockProductoRepository extends JpaRepository<StockProducto, Long> {
    Optional<StockProducto> findByProductoId(Long productoId);
}
