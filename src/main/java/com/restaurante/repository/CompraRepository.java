package com.restaurante.repository;
import com.restaurante.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByProveedorId(Long proveedorId);
    List<Compra> findByEstado(Compra.EstadoCompra estado);
}
