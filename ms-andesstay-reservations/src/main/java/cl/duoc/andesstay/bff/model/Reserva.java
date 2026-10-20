package cl.duoc.andesstay.bff.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
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

    public void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoReserva.CREADA;
        }
    }

    public void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}