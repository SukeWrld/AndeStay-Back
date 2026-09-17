package cl.duoc.andesstay.reservations.controller;

import cl.duoc.andesstay.reservations.model.EstadoReserva;
import cl.duoc.andesstay.reservations.model.Reserva;
import cl.duoc.andesstay.reservations.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    // ==================== CREAR RESERVA ====================
    // POST /api/reservations
    @PostMapping
    public ResponseEntity<Reserva> crear(@RequestBody Reserva reserva) {
        Reserva creada = reservaService.crear(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // ==================== OBTENER POR ID ====================
    // GET /api/reservations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Reserva> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    // ==================== LISTAR CON FILTROS ====================
    // GET /api/reservations?status=CONFIRMADA&from=2025-01-01&to=2025-01-31
    @GetMapping
    public ResponseEntity<List<Reserva>> listar(
            @RequestParam(required = false) EstadoReserva status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<Reserva> reservas = reservaService.listar(status, from, to);
        return ResponseEntity.ok(reservas);
    }

    // ==================== CAMBIAR ESTADO ====================
    // PUT /api/reservations/{id}/status
    // Body: { "status": "CONFIRMADA" }
    @PutMapping("/{id}/status")
    public ResponseEntity<Reserva> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        EstadoReserva nuevoEstado;
        try {
            nuevoEstado = EstadoReserva.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        // Por ahora el usuario lo dejamos hardcodeado.
        // Más adelante lo sacaremos del JWT (SecurityContext)
        String usuario = "sistema";

        Reserva actualizada = reservaService.cambiarEstado(id, nuevoEstado, usuario);
        return ResponseEntity.ok(actualizada);
    }

    // ==================== CANCELAR (atajo) ====================
    // PUT /api/reservations/{id}/cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Reserva> cancelar(@PathVariable Long id) {
        String usuario = "sistema";
        Reserva cancelada = reservaService.cancelar(id, usuario);
        return ResponseEntity.ok(cancelada);
    }
}