package cl.duoc.andesstay.bff.dto;

import cl.duoc.andesstay.bff.model.EstadoReserva;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservaResponse {

    private String id;
    private Long unidadId;
    private String nombreHuesped;
    private String emailHuesped;
    private String telefonoHuesped;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private Integer cantidadPersonas;
    private EstadoReserva estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String creadoPor;
    private String actualizadoPor;
}