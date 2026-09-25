package cl.duoc.andesstay.bff.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CrearReservaRequest {

    @NotNull(message = "El ID de la unidad es obligatorio")
    private Long unidadId;

    @NotBlank(message = "El nombre del huésped es obligatorio")
    @Size(max = 100)
    private String nombreHuesped;

    @NotBlank(message = "El email del huésped es obligatorio")
    @Email(message = "El email no es válido")
    private String emailHuesped;

    @Size(max = 20)
    private String telefonoHuesped;

    @NotNull(message = "La fecha de entrada es obligatoria")
    private LocalDate fechaEntrada;

    @NotNull(message = "La fecha de salida es obligatoria")
    private LocalDate fechaSalida;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(value = 1, message = "Debe haber al menos 1 persona")
    private Integer cantidadPersonas;
}