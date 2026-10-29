package cl.duoc.andesstay.audit.service;

import cl.duoc.andesstay.audit.model.AuditEvent;
import cl.duoc.andesstay.audit.repository.AuditEventRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditTimelineService {
    private final AuditEventRepository repository;

    public AuditTimelineService(AuditEventRepository repository) {
        this.repository = repository;
    }

    public List<AuditEvent> search(String reservaId, String actor, String eventType,
                                   LocalDateTime from, LocalDateTime to) {
        return repository.findAll().stream()
                .filter(event -> reservaId == null || reservaId.equals(event.getReservaId()))
                .filter(event -> actor == null || actor.equalsIgnoreCase(event.getActor()))
                .filter(event -> eventType == null || eventType.equalsIgnoreCase(event.getEventType()))
                .filter(event -> from == null || !event.getOcurridoEn().isBefore(from))
                .filter(event -> to == null || !event.getOcurridoEn().isAfter(to))
                .sorted(Comparator.comparing(AuditEvent::getOcurridoEn).reversed())
                .toList();
    }
}
