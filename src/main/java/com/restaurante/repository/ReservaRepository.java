package com.restaurante.repository;
import com.restaurante.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByMesaIdAndEstado(Integer mesaId, Reserva.EstadoReserva estado);
    List<Reserva> findByEstado(Reserva.EstadoReserva estado);
}
