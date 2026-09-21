package cl.duoc.andesstay.reservations.repository;

import cl.duoc.andesstay.reservations.model.EstadoReserva;
import cl.duoc.andesstay.reservations.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByEstado(EstadoReserva estado);

    List<Reserva> findByFechaEntradaBetween(LocalDate from, LocalDate to);

    List<Reserva> findByEstadoAndFechaEntradaBetween(
            EstadoReserva estado,
            LocalDate from,
            LocalDate to
    );

    List<Reserva> findByUnidadId(Long unidadId);

    List<Reserva> findByEmailHuesped(String emailHuesped);

    @Query("""
        SELECT r FROM Reserva r
        WHERE r.unidadId = :unidadId
          AND r.estado NOT IN ('CANCELADA', 'CHECKOUT')
          AND r.fechaEntrada < :fechaSalida
          AND r.fechaSalida > :fechaEntrada
        """)
    List<Reserva> findReservasSolapadas(
            @Param("unidadId") Long unidadId,
            @Param("fechaEntrada") LocalDate fechaEntrada,
            @Param("fechaSalida") LocalDate fechaSalida
    );
}