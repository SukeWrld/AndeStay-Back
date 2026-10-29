package cl.duoc.andesstay.ms_andesstay_catalog.controller;

import cl.duoc.andesstay.ms_andesstay_catalog.dto.UnitRequest;
import cl.duoc.andesstay.ms_andesstay_catalog.model.Unit;
import cl.duoc.andesstay.ms_andesstay_catalog.service.UnitService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/units")
public class UnitController {
    private final UnitService service;

    public UnitController(UnitService service) {
        this.service = service;
    }

    @GetMapping
    public List<Unit> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Unit get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<Unit> create(@Valid @RequestBody UnitRequest request) {
        Unit created = service.create(request);
        return ResponseEntity.created(URI.create("/api/catalog/units/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public Unit update(@PathVariable long id, @Valid @RequestBody UnitRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
