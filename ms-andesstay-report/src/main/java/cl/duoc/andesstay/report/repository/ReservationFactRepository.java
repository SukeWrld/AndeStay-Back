package cl.duoc.andesstay.report.repository;

import cl.duoc.andesstay.report.model.ReservationFact;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservationFactRepository extends MongoRepository<ReservationFact, String> {
}
