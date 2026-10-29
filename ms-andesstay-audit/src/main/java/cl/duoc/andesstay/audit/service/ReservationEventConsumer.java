package cl.duoc.andesstay.audit.service;

import cl.duoc.andesstay.audit.model.AuditEvent;
import cl.duoc.andesstay.audit.repository.AuditEventRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventConsumer {
    private final ObjectMapper objectMapper;
    private final AuditEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public ReservationEventConsumer(ObjectMapper objectMapper, AuditEventRepository repository,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${andesstay.kafka.reservations-topic:reservations.events}",
            groupId = "${spring.kafka.consumer.group-id:andesstay-audit}")
    public void consume(String payload) throws Exception {
        JsonNode event = objectMapper.readTree(payload);
        String eventId = requiredText(event, "eventId");
        if (repository.existsById(eventId)) {
            return;
        }
        LocalDateTime occurredAt = event.hasNonNull("ocurridoEn")
                ? LocalDateTime.parse(event.get("ocurridoEn").asText())
                : LocalDateTime.now();
        kafkaTemplate.send("audit.timeline", eventId, payload).get();
        repository.save(new AuditEvent(
                eventId,
                requiredText(event, "reservaId"),
                requiredText(event, "eventType"),
                text(event, "actor"),
                text(event, "estado"),
                event.hasNonNull("unidadId") ? event.get("unidadId").asLong() : null,
                occurredAt,
                payload
        ));
    }

    private String requiredText(JsonNode event, String field) {
        String value = text(event, field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required Kafka event field is missing: " + field);
        }
        return value;
    }

    private String text(JsonNode event, String field) {
        return event.hasNonNull(field) ? event.get(field).asText() : null;
    }
}
