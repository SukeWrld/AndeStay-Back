package cl.duoc.andesstay.report.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "report_reservation_events")
public class ReservationFact {
    @Id
    private String eventId;

    private String reservaId;

    private String eventType;

    private String estado;

    private Long unidadId;

    private LocalDateTime ocurridoEn;

    private String payload;

    protected ReservationFact() {
    }

    public ReservationFact(String eventId, String reservaId, String eventType, String estado,
                           Long unidadId, LocalDateTime ocurridoEn, String payload) {
        this.eventId = eventId;
        this.reservaId = reservaId;
        this.eventType = eventType;
        this.estado = estado;
        this.unidadId = unidadId;
        this.ocurridoEn = ocurridoEn;
        this.payload = payload;
    }

    public String getEventId() { return eventId; }
    public String getReservaId() { return reservaId; }
    public String getEventType() { return eventType; }
    public String getEstado() { return estado; }
    public Long getUnidadId() { return unidadId; }
    public LocalDateTime getOcurridoEn() { return ocurridoEn; }
}
