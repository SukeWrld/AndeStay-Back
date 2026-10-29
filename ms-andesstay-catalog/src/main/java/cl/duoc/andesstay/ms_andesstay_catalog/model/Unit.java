package cl.duoc.andesstay.ms_andesstay_catalog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Document(collection = "catalog_units")
public class Unit {
    @Id
    private Long id;

    private String name;

    private String type;

    private int capacity;

    private BigDecimal nightlyRate;

    private boolean active = true;

    protected Unit() {
    }

    public Unit(Long id, String name, String type, int capacity, BigDecimal nightlyRate, boolean active) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.nightlyRate = nightlyRate;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getCapacity() { return capacity; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
    public boolean isActive() { return active; }

    public void update(String name, String type, int capacity, BigDecimal nightlyRate, boolean active) {
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.nightlyRate = nightlyRate;
        this.active = active;
    }
}
