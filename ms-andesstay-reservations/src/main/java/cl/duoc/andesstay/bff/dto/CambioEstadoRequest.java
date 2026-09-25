package cl.duoc.andesstay.bff.dto;

import cl.duoc.andesstay.bff.model.EstadoReserva;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambioEstadoRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoReserva status;
}