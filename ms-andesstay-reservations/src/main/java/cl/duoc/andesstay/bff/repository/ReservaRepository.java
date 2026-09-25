package cl.duoc.andesstay.bff.repository;

import cl.duoc.andesstay.bff.model.EstadoReserva;
import cl.duoc.andesstay.bff.model.Reserva;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends MongoRepository<Reserva, String> {

    List<Reserva> findByEstado(EstadoReserva estado);

    List<Reserva> findByFechaEntradaBetween(LocalDate from, LocalDate to);

    List<Reserva> findByEstadoAndFechaEntradaBetween(
            EstadoReserva estado,
            LocalDate from,
            LocalDate to
    );

    List<Reserva> findByUnidadId(Long unidadId);

    List<Reserva> findByEmailHuesped(String emailHuesped);

    @Query("{ 'unidadId': ?0, 'estado': { $nin: ['CANCELADA', 'CHECKOUT'] }, 'fechaEntrada': { $lt: ?2 }, 'fechaSalida': { $gt: ?1 } }")
    List<Reserva> findReservasSolapadas(
            Long unidadId,
            LocalDate fechaEntrada,
            LocalDate fechaSalida
    );
}