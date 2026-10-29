package cl.duoc.andesstay.ms_andesstay_notify.config;

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
public class RabbitTopologyConfig {
    public static final String DIRECT_EXCHANGE = "cmd.direct";
    public static final String TOPIC_EXCHANGE = "cmd.topic";
    public static final String DEAD_EXCHANGE = "cmd.dead.dlx";

    @Bean
    DirectExchange commandDirectExchange() {
        return new DirectExchange(DIRECT_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange commandTopicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_EXCHANGE, true, false);
    }

    @Bean
    Queue emailQueue() {
        return commandQueue("q.cmd.email", "q.cmd.email.dlq");
    }

    @Bean
    Queue housekeepingQueue() {
        return commandQueue("q.cmd.housekeeping", "q.cmd.housekeeping.dlq");
    }

    @Bean
    Queue voucherQueue() {
        return commandQueue("q.cmd.voucher", "q.cmd.voucher.dlq");
    }

    @Bean
    Queue emailDeadLetterQueue() {
        return QueueBuilder.durable("q.cmd.email.dlq").build();
    }

    @Bean
    Queue housekeepingDeadLetterQueue() {
        return QueueBuilder.durable("q.cmd.housekeeping.dlq").build();
    }

    @Bean
    Queue voucherDeadLetterQueue() {
        return QueueBuilder.durable("q.cmd.voucher.dlq").build();
    }

    @Bean
    Binding emailDirectBinding(@Qualifier("emailQueue") Queue emailQueue,
            @Qualifier("commandDirectExchange") DirectExchange commandDirectExchange) {
        return BindingBuilder.bind(emailQueue).to(commandDirectExchange).with("email.send");
    }

    @Bean
    Binding emailTopicBinding(@Qualifier("emailQueue") Queue emailQueue,
            @Qualifier("commandTopicExchange") TopicExchange commandTopicExchange) {
        return BindingBuilder.bind(emailQueue).to(commandTopicExchange).with("email.*");
    }

    @Bean
    Binding housekeepingDirectBinding(@Qualifier("housekeepingQueue") Queue housekeepingQueue,
            @Qualifier("commandDirectExchange") DirectExchange commandDirectExchange) {
        return BindingBuilder.bind(housekeepingQueue).to(commandDirectExchange).with("housekeeping.ticket");
    }

    @Bean
    Binding housekeepingTopicBinding(@Qualifier("housekeepingQueue") Queue housekeepingQueue,
            @Qualifier("commandTopicExchange") TopicExchange commandTopicExchange) {
        return BindingBuilder.bind(housekeepingQueue).to(commandTopicExchange).with("housekeeping.#");
    }

    @Bean
    Binding voucherDirectBinding(@Qualifier("voucherQueue") Queue voucherQueue,
            @Qualifier("commandDirectExchange") DirectExchange commandDirectExchange) {
        return BindingBuilder.bind(voucherQueue).to(commandDirectExchange).with("voucher.gen");
    }

    @Bean
    Binding voucherTopicBinding(@Qualifier("voucherQueue") Queue voucherQueue,
            @Qualifier("commandTopicExchange") TopicExchange commandTopicExchange) {
        return BindingBuilder.bind(voucherQueue).to(commandTopicExchange).with("voucher.*");
    }

    @Bean
    Binding emailDeadLetterBinding(@Qualifier("emailDeadLetterQueue") Queue emailDeadLetterQueue,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(emailDeadLetterQueue).to(deadLetterExchange).with("q.cmd.email.dlq");
    }

    @Bean
    Binding housekeepingDeadLetterBinding(@Qualifier("housekeepingDeadLetterQueue") Queue housekeepingDeadLetterQueue,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(housekeepingDeadLetterQueue).to(deadLetterExchange).with("q.cmd.housekeeping.dlq");
    }

    @Bean
    Binding voucherDeadLetterBinding(@Qualifier("voucherDeadLetterQueue") Queue voucherDeadLetterQueue,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(voucherDeadLetterQueue).to(deadLetterExchange).with("q.cmd.voucher.dlq");
    }

    private Queue commandQueue(String name, String deadLetterRoutingKey) {
        return QueueBuilder.durable(name)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }
}
