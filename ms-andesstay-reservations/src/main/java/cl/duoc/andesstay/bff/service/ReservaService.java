package cl.duoc.andesstay.reservations.service;

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

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;

    // ==================== CREAR ====================
    @Transactional
    public Reserva crear(Reserva reserva) {
        // Validación básica de fechas
        if (reserva.getFechaSalida().isBefore(reserva.getFechaEntrada()) ||
            reserva.getFechaSalida().isEqual(reserva.getFechaEntrada())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de salida debe ser posterior a la fecha de entrada");
        }

        // Validar que no haya solapamiento (overbooking)
        List<Reserva> solapadas = reservaRepository.findReservasSolapadas(
                reserva.getUnidadId(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida()
        );

        if (!solapadas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La unidad no está disponible en las fechas seleccionadas");
        }

        reserva.setEstado(EstadoReserva.CREADA);
        return reservaRepository.save(reserva);
    }

    // ==================== BUSCAR POR ID ====================
    @Transactional(readOnly = true)
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reserva no encontrada con id: " + id));
    }

    // ==================== LISTAR CON FILTROS ====================
    @Transactional(readOnly = true)
    public List<Reserva> listar(EstadoReserva estado, LocalDate from, LocalDate to) {
        if (estado != null && from != null && to != null) {
            return reservaRepository.findByEstadoAndFechaEntradaBetween(estado, from, to);
        }
        if (estado != null) {
            return reservaRepository.findByEstado(estado);
        }
        if (from != null && to != null) {
            return reservaRepository.findByFechaEntradaBetween(from, to);
        }
        return reservaRepository.findAll();
    }

    // ==================== CAMBIAR ESTADO ====================
    @Transactional
    public Reserva cambiarEstado(Long id, EstadoReserva nuevoEstado, String usuario) {
        Reserva reserva = obtenerPorId(id);
        EstadoReserva estadoActual = reserva.getEstado();

        // Validar transición de estados
        validarTransicion(estadoActual, nuevoEstado);

        reserva.setEstado(nuevoEstado);
        reserva.setActualizadoPor(usuario);

        return reservaRepository.save(reserva);
    }

    // ==================== CANCELAR ====================
    @Transactional
    public Reserva cancelar(Long id, String usuario) {
        return cambiarEstado(id, EstadoReserva.CANCELADA, usuario);
    }

    // ==================== VALIDACIÓN DE TRANSICIONES ====================
    private void validarTransicion(EstadoReserva actual, EstadoReserva nuevo) {
        // No se puede modificar una reserva ya finalizada
        if (actual == EstadoReserva.CHECKOUT || actual == EstadoReserva.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede cambiar el estado de una reserva finalizada o cancelada");
        }

        // Regla del caso: No se puede hacer check-in sin CONFIRMAR
        if (nuevo == EstadoReserva.CHECKIN_PENDIENTE || nuevo == EstadoReserva.EN_ESTADIA) {
            if (actual != EstadoReserva.CONFIRMADA && actual != EstadoReserva.CHECKIN_PENDIENTE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se puede hacer check-in. La reserva debe estar en estado CONFIRMADA");
            }
        }

        // Transiciones permitidas (simplificadas pero seguras)
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