package cl.duoc.andesstay.reservations.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AccesoKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AccesoKafkaConsumer.class);

    @KafkaListener(topics = "accesos", groupId = "andestay-group")
    public void consumirAcceso(String mensaje) {
        log.info("Mensaje recibido en topic 'accesos': {}", mensaje);
    }
}