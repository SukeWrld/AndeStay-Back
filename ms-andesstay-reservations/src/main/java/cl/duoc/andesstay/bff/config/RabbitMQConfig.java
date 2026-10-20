package cl.duoc.andesstay.bff.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RESERVAS_EXCHANGE = "andesstay.reservas";
    public static final String RESERVAS_QUEUE = "andesstay.reservas.events";
    public static final String RESERVA_CREADA_ROUTING_KEY = "reserva.creada";
    public static final String RESERVA_ESTADO_CAMBIADO_ROUTING_KEY = "reserva.estado-cambiado";

    @Bean
    public TopicExchange reservasExchange() {
        return new TopicExchange(RESERVAS_EXCHANGE, true, false);
    }

    @Bean
    public Queue reservasEventsQueue() {
        return new Queue(RESERVAS_QUEUE, true);
    }

    @Bean
    public Binding reservaCreadaBinding(Queue reservasEventsQueue, TopicExchange reservasExchange) {
        return BindingBuilder.bind(reservasEventsQueue)
                .to(reservasExchange)
                .with(RESERVA_CREADA_ROUTING_KEY);
    }

    @Bean
    public Binding reservaEstadoCambiadoBinding(Queue reservasEventsQueue, TopicExchange reservasExchange) {
        return BindingBuilder.bind(reservasEventsQueue)
                .to(reservasExchange)
                .with(RESERVA_ESTADO_CAMBIADO_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
