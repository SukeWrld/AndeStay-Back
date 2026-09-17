package cl.duoc.andesstay.reservations.dto;

import cl.duoc.andesstay.reservations.model.EstadoReserva;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambioEstadoRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoReserva status;
}