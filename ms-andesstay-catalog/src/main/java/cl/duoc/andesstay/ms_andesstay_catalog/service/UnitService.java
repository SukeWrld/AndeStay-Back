package cl.duoc.andesstay.ms_andesstay_catalog.service;

import cl.duoc.andesstay.ms_andesstay_catalog.dto.UnitRequest;
import cl.duoc.andesstay.ms_andesstay_catalog.model.Unit;
import cl.duoc.andesstay.ms_andesstay_catalog.repository.UnitRepository;
import java.util.List;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UnitService {
    private final UnitRepository repository;
    private final MongoTemplate mongoTemplate;

    public UnitService(UnitRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Unit> list() {
        return repository.findAll();
    }

    public Unit get(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
    }

    public Unit create(UnitRequest request) {
        Long id = mongoTemplate.findAndModify(
                Query.query(Criteria.where("_id").is("catalog_unit")),
                new Update().inc("sequence", 1),
                FindAndModifyOptions.options().upsert(true).returnNew(true),
                Sequence.class
        ).sequence;
        return repository.save(new Unit(id, request.name(), request.type(), request.capacity(),
                request.nightlyRate(), request.active() == null || request.active()));
    }

    public Unit update(long id, UnitRequest request) {
        Unit unit = get(id);
        unit.update(request.name(), request.type(), request.capacity(), request.nightlyRate(),
                request.active() == null || request.active());
        return repository.save(unit);
    }

    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada");
        }
        repository.deleteById(id);
    }

    private static class Sequence {
        private String id;
        private Long sequence;
    }
}
