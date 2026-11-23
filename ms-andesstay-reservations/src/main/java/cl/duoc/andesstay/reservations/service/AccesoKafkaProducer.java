package cl.duoc.andesstay.reservations.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccesoKafkaProducer {

    private static final String TOPIC = "accesos";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public AccesoKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void enviarAcceso(String clave, String mensaje) {
        kafkaTemplate.send(TOPIC, clave, mensaje);
    }
}