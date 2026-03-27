package com.restaurante.repository;
import com.restaurante.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface MesaRepository extends JpaRepository<Mesa, Integer> {
    List<Mesa> findByEstado(Mesa.EstadoMesa estado);
    List<Mesa> findByZona(String zona);
    Optional<Mesa> findByNumero(Integer numero);
    List<Mesa> findAllByOrderByNumeroAsc();
}
