package com.restaurante.repository;
import com.restaurante.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByEstado(Producto.EstadoProducto estado);
    List<Producto> findByCategoriaId(Integer categoriaId);
    List<Producto> findByCategoriaIdAndEstado(Integer categoriaId, Producto.EstadoProducto estado);
    @Query("SELECT p FROM Producto p WHERE p.estado = 'ACTIVO' ORDER BY p.categoria.ordenMenu, p.nombre")
    List<Producto> findAllActivos();
}
