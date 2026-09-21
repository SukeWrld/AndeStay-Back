package cl.duoc.andesstay.reservations.mapper;

import cl.duoc.andesstay.reservations.dto.CrearReservaRequest;
import cl.duoc.andesstay.reservations.dto.ReservaResponse;
import cl.duoc.andesstay.reservations.model.Reserva;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public Reserva toEntity(CrearReservaRequest request) {
        return Reserva.builder()
                .unidadId(request.getUnidadId())
                .nombreHuesped(request.getNombreHuesped())
                .emailHuesped(request.getEmailHuesped())
                .telefonoHuesped(request.getTelefonoHuesped())
                .fechaEntrada(request.getFechaEntrada())
                .fechaSalida(request.getFechaSalida())
                .cantidadPersonas(request.getCantidadPersonas())
                .build();
    }

    public ReservaResponse toResponse(Reserva reserva) {
        return ReservaResponse.builder()
                .id(reserva.getId())
                .unidadId(reserva.getUnidadId())
                .nombreHuesped(reserva.getNombreHuesped())
                .emailHuesped(reserva.getEmailHuesped())
                .telefonoHuesped(reserva.getTelefonoHuesped())
                .fechaEntrada(reserva.getFechaEntrada())
                .fechaSalida(reserva.getFechaSalida())
                .cantidadPersonas(reserva.getCantidadPersonas())
                .estado(reserva.getEstado())
                .fechaCreacion(reserva.getFechaCreacion())
                .fechaActualizacion(reserva.getFechaActualizacion())
                .creadoPor(reserva.getCreadoPor())
                .actualizadoPor(reserva.getActualizadoPor())
                .build();
    }
}