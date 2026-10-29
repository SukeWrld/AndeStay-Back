package cl.duoc.andesstay.report.service;

import cl.duoc.andesstay.report.model.ReservationFact;
import cl.duoc.andesstay.report.repository.ReservationFactRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
    private final ReservationFactRepository repository;

    public ReportService(ReservationFactRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> kpis(String range) {
        List<ReservationFact> allFacts = repository.findAll();
        List<ReservationFact> facts = factsSince(allFacts, range);
        Map<String, ReservationFact> latest = latestByReservation(allFacts);
        Map<String, Long> byHour = facts.stream()
                .filter(fact -> "RESERVA_CREADA".equals(fact.getEventType()))
                .collect(Collectors.groupingBy(fact -> fact.getOcurridoEn().format(HOUR),
                        java.util.TreeMap::new, Collectors.counting()));
        double averageCycleHours = averageCycleHours(allFacts, facts);
        return Map.of(
                "range", range,
                "reservationsCreated", facts.stream().filter(f -> "RESERVA_CREADA".equals(f.getEventType())).count(),
                "activeOccupancy", latest.values().stream()
                        .filter(f -> "EN_ESTADIA".equals(f.getEstado()))
                        .count(),
                "averageCycleHours", averageCycleHours,
                "reservationsByHour", byHour
        );
    }

    public List<Map<String, Object>> topUnits(String range) {
        return factsSince(repository.findAll(), range).stream()
                .filter(fact -> "RESERVA_CREADA".equals(fact.getEventType()) && fact.getUnidadId() != null)
                .collect(Collectors.groupingBy(ReservationFact::getUnidadId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(entry -> Map.<String, Object>of("unidadId", entry.getKey(), "reservations", entry.getValue()))
                .toList();
    }

    private List<ReservationFact> factsSince(List<ReservationFact> facts, String range) {
        LocalDateTime cutoff = switch (range) {
            case "last24h" -> LocalDateTime.now().minusHours(24);
            case "last7d" -> LocalDateTime.now().minusDays(7);
            case "last30d" -> LocalDateTime.now().minusDays(30);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported range: " + range);
        };
        return facts.stream().filter(fact -> !fact.getOcurridoEn().isBefore(cutoff)).toList();
    }

    private Map<String, ReservationFact> latestByReservation(List<ReservationFact> facts) {
        Map<String, ReservationFact> latest = new HashMap<>();
        facts.stream().sorted(Comparator.comparing(ReservationFact::getOcurridoEn))
                .forEach(fact -> latest.put(fact.getReservaId(), fact));
        return latest;
    }

    private double averageCycleHours(List<ReservationFact> allFacts, List<ReservationFact> rangeFacts) {
        Map<String, LocalDateTime> created = allFacts.stream()
                .filter(fact -> "RESERVA_CREADA".equals(fact.getEventType()))
                .collect(Collectors.toMap(ReservationFact::getReservaId, ReservationFact::getOcurridoEn,
                        (first, ignored) -> first));
        return rangeFacts.stream()
                .filter(fact -> "CHECKOUT".equals(fact.getEstado()))
                .map(fact -> {
                    LocalDateTime start = created.get(fact.getReservaId());
                    return start == null ? null : Duration.between(start, fact.getOcurridoEn()).toMinutes() / 60.0;
                })
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
