package cl.duoc.andesstay.reservations.service;

import cl.duoc.andesstay.reservations.config.RabbitMQConfig;
import cl.duoc.andesstay.reservations.dto.ReservaEvento;
import cl.duoc.andesstay.reservations.model.EstadoReserva;
import cl.duoc.andesstay.reservations.model.Reserva;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReservaEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, ReservaEvento> kafkaTemplate;

    public void publicarCreada(Reserva reserva, String actor) {
        publicar(reserva, null, reserva.getEstado(), "RESERVA_CREADA",
                RabbitMQConfig.RESERVA_CREADA_ROUTING_KEY, actor);
    }

    public void publicarEstadoCambiado(Reserva reserva, EstadoReserva estadoAnterior, String actor) {
        publicar(reserva, estadoAnterior, reserva.getEstado(), "RESERVA_ESTADO_CAMBIADO",
                RabbitMQConfig.RESERVA_ESTADO_CAMBIADO_ROUTING_KEY, actor);
    }

    private void publicar(
            Reserva reserva,
            EstadoReserva estadoAnterior,
            EstadoReserva estado,
            String eventType,
            String routingKey,
            String actor
    ) {
        ReservaEvento evento = ReservaEvento.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .actor(actor)
                .reservaId(reserva.getId())
                .unidadId(reserva.getUnidadId())
                .estadoAnterior(estadoAnterior)
                .estado(estado)
                .fechaEntrada(reserva.getFechaEntrada())
                .fechaSalida(reserva.getFechaSalida())
                .ocurridoEn(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.RESERVAS_EXCHANGE, routingKey, evento);
        kafkaTemplate.send("reservations.events", evento.getReservaId(), evento);
        String subject = "RESERVA_CREADA".equals(eventType)
                ? "Comprobante de reserva AndeStay"
                : "Actualizacion de tu reserva AndeStay";
        rabbitTemplate.convertAndSend("cmd.direct", "email.send", Map.ofEntries(
                Map.entry("eventId", evento.getEventId()),
                Map.entry("correlationId", evento.getEventId()),
                Map.entry("to", reserva.getEmailHuesped()),
                Map.entry("subject", subject),
                Map.entry("body", "Tu reserva " + reserva.getId() + " cambio al estado " + estado + "."),
                Map.entry("reservaId", reserva.getId()),
                Map.entry("nombreHuesped", reserva.getNombreHuesped() == null ? "Huesped AndeStay" : reserva.getNombreHuesped()),
                Map.entry("unidadId", String.valueOf(reserva.getUnidadId())),
                Map.entry("fechaEntrada", reserva.getFechaEntrada() == null ? "No informada" : reserva.getFechaEntrada().toString()),
                Map.entry("fechaSalida", reserva.getFechaSalida() == null ? "No informada" : reserva.getFechaSalida().toString()),
                Map.entry("cantidadPersonas", reserva.getCantidadPersonas() == null ? "No informado" : reserva.getCantidadPersonas().toString()),
                Map.entry("estado", estado.name()),
                Map.entry("eventType", eventType)
        ));
        if (estado == EstadoReserva.CONFIRMADA) {
            rabbitTemplate.convertAndSend("cmd.direct", "housekeeping.ticket", Map.of(
                    "eventId", evento.getEventId(),
                    "correlationId", evento.getEventId(),
                    "reservaId", reserva.getId(),
                    "unidadId", reserva.getUnidadId(),
                    "fechaEntrada", reserva.getFechaEntrada().toString(),
                    "fechaSalida", reserva.getFechaSalida().toString()
            ));
        }
    }
}
