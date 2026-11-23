package cl.duoc.andesstay.reservations.controller;

import cl.duoc.andesstay.reservations.service.AccesoKafkaProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/kafka/accesos")
public class AccesoKafkaController {

    private final AccesoKafkaProducer producer;

    public AccesoKafkaController(AccesoKafkaProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> publicar(@RequestBody Map<String, String> body) {
        String clave = body.getOrDefault("clave", "reserva");
        String mensaje = body.getOrDefault("mensaje", "Acceso de prueba AndesStay");
        producer.enviarAcceso(clave, mensaje);
        return ResponseEntity.ok("Publicado en topic accesos");
    }
}