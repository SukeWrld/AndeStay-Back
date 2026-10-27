package cl.duoc.andesstay.reservations.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class RabbitMQConfig {

    public static final String RESERVAS_EXCHANGE = "andesstay.reservas";
    public static final String RESERVAS_QUEUE = "andesstay.reservas.events";
    public static final String RESERVA_CREADA_ROUTING_KEY = "reserva.creada";
    public static final String RESERVA_ESTADO_CAMBIADO_ROUTING_KEY = "reserva.estado-cambiado";

    @Bean
    public ConnectionFactory rabbitConnectionFactory(@Value("${cloudamqp.url}") URI cloudAmqpUri)
            throws Exception {
        if (!"amqps".equalsIgnoreCase(cloudAmqpUri.getScheme())) {
            throw new IllegalArgumentException("CloudAMQP must use an amqps:// URL");
        }

        com.rabbitmq.client.ConnectionFactory rabbitClientConnectionFactory =
                new com.rabbitmq.client.ConnectionFactory();
        rabbitClientConnectionFactory.setUri(cloudAmqpUri);
        return new CachingConnectionFactory(rabbitClientConnectionFactory);
    }

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
