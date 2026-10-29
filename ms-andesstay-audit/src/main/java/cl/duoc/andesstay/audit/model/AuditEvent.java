package cl.duoc.andesstay.audit.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "audit_events")
public class AuditEvent {
    @Id
    private String eventId;

    private String reservaId;

    private String eventType;

    private String actor;

    private String estado;

    private Long unidadId;

    private LocalDateTime ocurridoEn;

    private String payload;

    protected AuditEvent() {
    }

    public AuditEvent(String eventId, String reservaId, String eventType, String actor,
                      String estado, Long unidadId, LocalDateTime ocurridoEn, String payload) {
        this.eventId = eventId;
        this.reservaId = reservaId;
        this.eventType = eventType;
        this.actor = actor;
        this.estado = estado;
        this.unidadId = unidadId;
        this.ocurridoEn = ocurridoEn;
        this.payload = payload;
    }

    public String getEventId() { return eventId; }
    public String getReservaId() { return reservaId; }
    public String getEventType() { return eventType; }
    public String getActor() { return actor; }
    public String getEstado() { return estado; }
    public Long getUnidadId() { return unidadId; }
    public LocalDateTime getOcurridoEn() { return ocurridoEn; }
    public String getPayload() { return payload; }
}
