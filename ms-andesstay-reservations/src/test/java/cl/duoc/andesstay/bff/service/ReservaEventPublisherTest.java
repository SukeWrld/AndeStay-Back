package cl.duoc.andesstay.bff.service;

import cl.duoc.andesstay.bff.config.RabbitMQConfig;
import cl.duoc.andesstay.bff.dto.ReservaEvento;
import cl.duoc.andesstay.bff.model.EstadoReserva;
import cl.duoc.andesstay.bff.model.Reserva;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentCaptor.forClass;

class ReservaEventPublisherTest {

    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final ReservaEventPublisher publisher = new ReservaEventPublisher(rabbitTemplate);

    @Test
    void publicaEventoDeReservaCreada() {
        Reserva reserva = reserva(EstadoReserva.CREADA);
        var eventoCaptor = forClass(ReservaEvento.class);

        publisher.publicarCreada(reserva);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.RESERVAS_EXCHANGE),
                eq(RabbitMQConfig.RESERVA_CREADA_ROUTING_KEY),
                eventoCaptor.capture()
        );
        ReservaEvento evento = eventoCaptor.getValue();
        assertEquals("RESERVA_CREADA", evento.getEventType());
        assertEquals("reserva-123", evento.getReservaId());
        assertNull(evento.getEstadoAnterior());
        assertEquals(EstadoReserva.CREADA, evento.getEstado());
    }

    @Test
    void publicaEventoConEstadosAnteriorYNuevo() {
        Reserva reserva = reserva(EstadoReserva.CONFIRMADA);
        var eventoCaptor = forClass(ReservaEvento.class);

        publisher.publicarEstadoCambiado(reserva, EstadoReserva.CREADA);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.RESERVAS_EXCHANGE),
                eq(RabbitMQConfig.RESERVA_ESTADO_CAMBIADO_ROUTING_KEY),
                eventoCaptor.capture()
        );
        ReservaEvento evento = eventoCaptor.getValue();
        assertEquals("RESERVA_ESTADO_CAMBIADO", evento.getEventType());
        assertEquals(EstadoReserva.CREADA, evento.getEstadoAnterior());
        assertEquals(EstadoReserva.CONFIRMADA, evento.getEstado());
    }

    private Reserva reserva(EstadoReserva estado) {
        return Reserva.builder()
                .id("reserva-123")
                .unidadId(42L)
                .estado(estado)
                .fechaEntrada(LocalDate.of(2026, 11, 1))
                .fechaSalida(LocalDate.of(2026, 11, 5))
                .build();
    }
}
