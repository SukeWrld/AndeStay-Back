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

    // Buscar por estado
    List<Reserva> findByEstado(EstadoReserva estado);

    // Buscar por rango de fechas de entrada
    List<Reserva> findByFechaEntradaBetween(LocalDate from, LocalDate to);

    // Buscar por estado + rango de fechas
    List<Reserva> findByEstadoAndFechaEntradaBetween(
            EstadoReserva estado,
            LocalDate from,
            LocalDate to
    );

    // Buscar reservas de una unidad específica
    List<Reserva> findByUnidadId(Long unidadId);

    // Buscar reservas de un huésped por email
    List<Reserva> findByEmailHuesped(String emailHuesped);

    // Consulta personalizada: reservas activas de una unidad en un rango de fechas
    // (útil para validar disponibilidad)
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