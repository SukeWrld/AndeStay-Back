package cl.duoc.andesstay.reservations.service;

import cl.duoc.andesstay.reservations.dto.CrearReservaRequest;
import cl.duoc.andesstay.reservations.dto.ReservaResponse;
import cl.duoc.andesstay.reservations.mapper.ReservaMapper;
import cl.duoc.andesstay.reservations.model.EstadoReserva;
import cl.duoc.andesstay.reservations.model.Reserva;
import cl.duoc.andesstay.reservations.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;

    @Transactional
    public ReservaResponse crear(CrearReservaRequest request, String usuario) {
        if (request.getFechaSalida().isBefore(request.getFechaEntrada()) ||
            request.getFechaSalida().isEqual(request.getFechaEntrada())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de salida debe ser posterior a la fecha de entrada");
        }

        List<Reserva> solapadas = reservaRepository.findReservasSolapadas(
                request.getUnidadId(),
                request.getFechaEntrada(),
                request.getFechaSalida()
        );

        if (!solapadas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La unidad no está disponible en las fechas seleccionadas");
        }

        Reserva reserva = reservaMapper.toEntity(request);
        reserva.setEstado(EstadoReserva.CREADA);
        reserva.setCreadoPor(usuario);

        Reserva guardada = reservaRepository.save(reserva);
        return reservaMapper.toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtenerPorId(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reserva no encontrada con id: " + id));
        return reservaMapper.toResponse(reserva);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listar(EstadoReserva estado, LocalDate from, LocalDate to) {
        List<Reserva> reservas;

        if (estado != null && from != null && to != null) {
            reservas = reservaRepository.findByEstadoAndFechaEntradaBetween(estado, from, to);
        } else if (estado != null) {
            reservas = reservaRepository.findByEstado(estado);
        } else if (from != null && to != null) {
            reservas = reservaRepository.findByFechaEntradaBetween(from, to);
        } else {
            reservas = reservaRepository.findAll();
        }

        return reservas.stream()
                .map(reservaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservaResponse cambiarEstado(Long id, EstadoReserva nuevoEstado, String usuario) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reserva no encontrada con id: " + id));

        validarTransicion(reserva.getEstado(), nuevoEstado);

        reserva.setEstado(nuevoEstado);
        reserva.setActualizadoPor(usuario);

        Reserva actualizada = reservaRepository.save(reserva);
        return reservaMapper.toResponse(actualizada);
    }

    @Transactional
    public ReservaResponse cancelar(Long id, String usuario) {
        return cambiarEstado(id, EstadoReserva.CANCELADA, usuario);
    }

    private void validarTransicion(EstadoReserva actual, EstadoReserva nuevo) {
        if (actual == EstadoReserva.CHECKOUT || actual == EstadoReserva.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede cambiar el estado de una reserva finalizada o cancelada");
        }

        if ((nuevo == EstadoReserva.CHECKIN_PENDIENTE || nuevo == EstadoReserva.EN_ESTADIA)
                && actual != EstadoReserva.CONFIRMADA && actual != EstadoReserva.CHECKIN_PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede hacer check-in. La reserva debe estar en estado CONFIRMADA");
        }

        boolean esValida = switch (actual) {
            case CREADA -> nuevo == EstadoReserva.CONFIRMADA || nuevo == EstadoReserva.CANCELADA;
            case CONFIRMADA -> nuevo == EstadoReserva.CHECKIN_PENDIENTE
                    || nuevo == EstadoReserva.EN_ESTADIA
                    || nuevo == EstadoReserva.CANCELADA;
            case CHECKIN_PENDIENTE -> nuevo == EstadoReserva.EN_ESTADIA
                    || nuevo == EstadoReserva.CANCELADA;
            case EN_ESTADIA -> nuevo == EstadoReserva.CHECKOUT;
            default -> false;
        };

        if (!esValida) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transición de estado no permitida: " + actual + " → " + nuevo);
        }
    }
}