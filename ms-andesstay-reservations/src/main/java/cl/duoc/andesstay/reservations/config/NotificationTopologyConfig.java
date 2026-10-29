package cl.duoc.andesstay.reservations.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationTopologyConfig {
    @Bean
    DirectExchange commandDirectExchange() {
        return new DirectExchange("cmd.direct", true, false);
    }

    @Bean
    TopicExchange commandTopicExchange() {
        return new TopicExchange("cmd.topic", true, false);
    }

    @Bean
    DirectExchange commandDeadLetterExchange() {
        return new DirectExchange("cmd.dead.dlx", true, false);
    }

    @Bean
    Queue emailCommandQueue() {
        return commandQueue("q.cmd.email", "q.cmd.email.dlq");
    }

    @Bean
    Queue housekeepingCommandQueue() {
        return commandQueue("q.cmd.housekeeping", "q.cmd.housekeeping.dlq");
    }

    @Bean
    Queue voucherCommandQueue() {
        return commandQueue("q.cmd.voucher", "q.cmd.voucher.dlq");
    }

    @Bean
    Queue emailCommandDlq() {
        return QueueBuilder.durable("q.cmd.email.dlq").build();
    }

    @Bean
    Queue housekeepingCommandDlq() {
        return QueueBuilder.durable("q.cmd.housekeeping.dlq").build();
    }

    @Bean
    Queue voucherCommandDlq() {
        return QueueBuilder.durable("q.cmd.voucher.dlq").build();
    }

    @Bean
    Binding emailCommandDirectBinding(@Qualifier("emailCommandQueue") Queue queue,
            @Qualifier("commandDirectExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("email.send");
    }

    @Bean
    Binding emailCommandTopicBinding(@Qualifier("emailCommandQueue") Queue queue,
            @Qualifier("commandTopicExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("email.*");
    }

    @Bean
    Binding housekeepingCommandDirectBinding(@Qualifier("housekeepingCommandQueue") Queue queue,
            @Qualifier("commandDirectExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("housekeeping.ticket");
    }

    @Bean
    Binding housekeepingCommandTopicBinding(@Qualifier("housekeepingCommandQueue") Queue queue,
            @Qualifier("commandTopicExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("housekeeping.#");
    }

    @Bean
    Binding voucherCommandDirectBinding(@Qualifier("voucherCommandQueue") Queue queue,
            @Qualifier("commandDirectExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("voucher.gen");
    }

    @Bean
    Binding voucherCommandTopicBinding(@Qualifier("voucherCommandQueue") Queue queue,
            @Qualifier("commandTopicExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("voucher.*");
    }

    @Bean
    Binding emailCommandDeadBinding(@Qualifier("emailCommandDlq") Queue queue,
            @Qualifier("commandDeadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("q.cmd.email.dlq");
    }

    @Bean
    Binding housekeepingCommandDeadBinding(@Qualifier("housekeepingCommandDlq") Queue queue,
            @Qualifier("commandDeadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("q.cmd.housekeeping.dlq");
    }

    @Bean
    Binding voucherCommandDeadBinding(@Qualifier("voucherCommandDlq") Queue queue,
            @Qualifier("commandDeadLetterExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("q.cmd.voucher.dlq");
    }

    private Queue commandQueue(String name, String deadLetterRoutingKey) {
        return QueueBuilder.durable(name)
                .deadLetterExchange("cmd.dead.dlx")
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }
}
