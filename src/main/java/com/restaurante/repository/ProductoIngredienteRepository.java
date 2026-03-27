package com.restaurante.repository;
import com.restaurante.entity.ProductoIngrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProductoIngredienteRepository extends JpaRepository<ProductoIngrediente, Long> {
    List<ProductoIngrediente> findByProductoId(Long productoId);
    void deleteByProductoId(Long productoId);
}
