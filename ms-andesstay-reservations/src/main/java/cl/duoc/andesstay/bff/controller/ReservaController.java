package cl.duoc.andesstay.bff.controller;

import cl.duoc.andesstay.bff.dto.CambioEstadoRequest;
import cl.duoc.andesstay.bff.dto.CrearReservaRequest;
import cl.duoc.andesstay.bff.dto.ReservaResponse;
import cl.duoc.andesstay.bff.model.EstadoReserva;
import cl.duoc.andesstay.bff.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody CrearReservaRequest request) {
        // Por ahora hardcodeado. Después lo sacamos del JWT
        String usuario = "sistema";
        ReservaResponse response = reservaService.crear(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listar(
            @RequestParam(required = false) EstadoReserva status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(reservaService.listar(status, from, to));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReservaResponse> cambiarEstado(
            @PathVariable String id,
            @Valid @RequestBody CambioEstadoRequest request
    ) {
        String usuario = "sistema";
        ReservaResponse response = reservaService.cambiarEstado(id, request.getStatus(), usuario);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservaResponse> cancelar(@PathVariable String id) {
        String usuario = "sistema";
        return ResponseEntity.ok(reservaService.cancelar(id, usuario));
    }
}