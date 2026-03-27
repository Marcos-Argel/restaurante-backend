package com.restaurante.repository;
import com.restaurante.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    @Query("SELECT i FROM Inventario i WHERE i.stockActual <= i.stockAlerta")
    List<Inventario> findStockBajo();

    @Query("SELECT i FROM Inventario i WHERE i.stockActual <= i.stockMinimo")
    List<Inventario> findStockCritico();

    @Query("SELECT i FROM Inventario i WHERE i.fechaVencimiento IS NOT NULL AND i.fechaVencimiento <= :fecha")
    List<Inventario> findProximosAVencer(LocalDate fecha);
}
