package cl.duoc.andesstay.reservations.dto;

import cl.duoc.andesstay.reservations.model.EstadoReserva;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class ReservaEvento {

    String eventId;
    String eventType;
    String reservaId;
    Long unidadId;
    EstadoReserva estadoAnterior;
    EstadoReserva estado;
    LocalDate fechaEntrada;
    LocalDate fechaSalida;
    LocalDateTime ocurridoEn;
}
