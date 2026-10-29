package cl.duoc.andesstay.audit.repository;

import cl.duoc.andesstay.audit.model.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
}
