package cl.duoc.andesstay.ms_andesstay_catalog.repository;

import cl.duoc.andesstay.ms_andesstay_catalog.model.Unit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UnitRepository extends MongoRepository<Unit, Long> {
}
