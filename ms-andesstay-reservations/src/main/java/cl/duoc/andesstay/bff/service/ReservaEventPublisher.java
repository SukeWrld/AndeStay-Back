package cl.duoc.andesstay.bff.service;

import cl.duoc.andesstay.bff.config.RabbitMQConfig;
import cl.duoc.andesstay.bff.dto.ReservaEvento;
import cl.duoc.andesstay.bff.model.EstadoReserva;
import cl.duoc.andesstay.bff.model.Reserva;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReservaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicarCreada(Reserva reserva) {
        publicar(reserva, null, reserva.getEstado(), "RESERVA_CREADA",
                RabbitMQConfig.RESERVA_CREADA_ROUTING_KEY);
    }

    public void publicarEstadoCambiado(Reserva reserva, EstadoReserva estadoAnterior) {
        publicar(reserva, estadoAnterior, reserva.getEstado(), "RESERVA_ESTADO_CAMBIADO",
                RabbitMQConfig.RESERVA_ESTADO_CAMBIADO_ROUTING_KEY);
    }

    private void publicar(
            Reserva reserva,
            EstadoReserva estadoAnterior,
            EstadoReserva estado,
            String eventType,
            String routingKey
    ) {
        ReservaEvento evento = ReservaEvento.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .reservaId(reserva.getId())
                .unidadId(reserva.getUnidadId())
                .estadoAnterior(estadoAnterior)
                .estado(estado)
                .fechaEntrada(reserva.getFechaEntrada())
                .fechaSalida(reserva.getFechaSalida())
                .ocurridoEn(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.RESERVAS_EXCHANGE, routingKey, evento);
    }
}
