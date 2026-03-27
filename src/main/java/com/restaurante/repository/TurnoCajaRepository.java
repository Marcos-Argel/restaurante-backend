package com.restaurante.repository;
import com.restaurante.entity.TurnoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TurnoCajaRepository extends JpaRepository<TurnoCaja, Long> {
    Optional<TurnoCaja> findByUsuarioIdAndEstado(Integer usuarioId, TurnoCaja.EstadoTurno estado);
    List<TurnoCaja> findByEstado(TurnoCaja.EstadoTurno estado);
}
