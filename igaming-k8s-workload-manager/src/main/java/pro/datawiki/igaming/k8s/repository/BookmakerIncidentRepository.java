package pro.datawiki.igaming.k8s.repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.k8s.dto.BookmakerIncident;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BookmakerIncidentRepository {

    private final JdbcTemplate jdbcTemplate;

    // In-memory cache / fallback
    private final Map<String, BookmakerIncident> memoryStore = new ConcurrentHashMap<>();
    private final AtomicLong fallbackIdGenerator = new AtomicLong(1000);
    private volatile boolean dbAvailable = false;

    private static final RowMapper<BookmakerIncident> ROW_MAPPER = (rs, rowNum) -> {
        Timestamp detectedTs = rs.getTimestamp("detected_at");
        Timestamp lastSeenTs = rs.getTimestamp("last_seen_at");
        Timestamp resolvedTs = rs.getTimestamp("resolved_at");

        return BookmakerIncident.builder()
                .id(rs.getLong("id"))
                .category(rs.getString("category"))
                .target(rs.getString("target"))
                .severity(rs.getString("severity"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .currentValue(rs.getString("current_value"))
                .thresholdValue(rs.getString("threshold_value"))
                .status(rs.getString("status"))
                .planeIssueId(rs.getString("plane_issue_id"))
                .planeIssueUrl(rs.getString("plane_issue_url"))
                .aiTaskId((Long) rs.getObject("ai_task_id"))
                .detectedAt(detectedTs != null ? detectedTs.toInstant() : null)
                .lastSeenAt(lastSeenTs != null ? lastSeenTs.toInstant() : null)
                .resolvedAt(resolvedTs != null ? resolvedTs.toInstant() : null)
                .build();
    };

    @PostConstruct
    public void initTable() {
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS bookmaker_incidents (" +
                    "  id BIGSERIAL PRIMARY KEY, " +
                    "  category VARCHAR(64) NOT NULL, " +
                    "  target VARCHAR(128) NOT NULL, " +
                    "  severity VARCHAR(32) NOT NULL, " +
                    "  title VARCHAR(255) NOT NULL, " +
                    "  description TEXT, " +
                    "  current_value VARCHAR(128), " +
                    "  threshold_value VARCHAR(128), " +
                    "  status VARCHAR(32) NOT NULL, " +
                    "  plane_issue_id VARCHAR(128), " +
                    "  plane_issue_url VARCHAR(512), " +
                    "  ai_task_id BIGINT, " +
                    "  detected_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(), " +
                    "  last_seen_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(), " +
                    "  resolved_at TIMESTAMP WITH TIME ZONE" +
                    ");"
            );
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_b_incidents_status ON bookmaker_incidents(status);");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_b_incidents_cat_target ON bookmaker_incidents(category, target);");
            dbAvailable = true;
            log.info("✅ bookmaker_incidents table verified in PostgreSQL");
        } catch (Exception e) {
            log.warn("PostgreSQL not immediately ready for bookmaker_incidents ({}), using in-memory store", e.getMessage());
            dbAvailable = false;
        }
    }

    public Optional<BookmakerIncident> findOpenIncident(String category, String target) {
        String key = category + ":" + target;
        if (dbAvailable) {
            try {
                String sql = "SELECT * FROM bookmaker_incidents WHERE category = ? AND target = ? AND status = 'OPEN' ORDER BY id DESC LIMIT 1";
                List<BookmakerIncident> list = jdbcTemplate.query(sql, ROW_MAPPER, category, target);
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                log.warn("Failed to query open incident from DB: {}", e.getMessage());
            }
        }
        BookmakerIncident mem = memoryStore.get(key);
        if (mem != null && "OPEN".equalsIgnoreCase(mem.getStatus())) {
            return Optional.of(mem);
        }
        return Optional.empty();
    }

    public BookmakerIncident save(BookmakerIncident incident) {
        String key = incident.getCategory() + ":" + incident.getTarget();
        if (incident.getDetectedAt() == null) {
            incident.setDetectedAt(Instant.now());
        }
        incident.setLastSeenAt(Instant.now());

        if (dbAvailable) {
            try {
                if (incident.getId() == null) {
                    String sql = "INSERT INTO bookmaker_incidents (category, target, severity, title, description, " +
                            "current_value, threshold_value, status, plane_issue_id, plane_issue_url, ai_task_id, " +
                            "detected_at, last_seen_at, resolved_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

                    Long generatedId = jdbcTemplate.queryForObject(sql, Long.class,
                            incident.getCategory(),
                            incident.getTarget(),
                            incident.getSeverity(),
                            incident.getTitle(),
                            incident.getDescription(),
                            incident.getCurrentValue(),
                            incident.getThresholdValue(),
                            incident.getStatus(),
                            incident.getPlaneIssueId(),
                            incident.getPlaneIssueUrl(),
                            incident.getAiTaskId(),
                            incident.getDetectedAt() != null ? Timestamp.from(incident.getDetectedAt()) : null,
                            incident.getLastSeenAt() != null ? Timestamp.from(incident.getLastSeenAt()) : null,
                            incident.getResolvedAt() != null ? Timestamp.from(incident.getResolvedAt()) : null
                    );
                    incident.setId(generatedId);
                } else {
                    String sql = "UPDATE bookmaker_incidents SET " +
                            "severity = ?, title = ?, description = ?, current_value = ?, threshold_value = ?, " +
                            "status = ?, plane_issue_id = ?, plane_issue_url = ?, ai_task_id = ?, " +
                            "last_seen_at = ?, resolved_at = ? " +
                            "WHERE id = ?";

                    jdbcTemplate.update(sql,
                            incident.getSeverity(),
                            incident.getTitle(),
                            incident.getDescription(),
                            incident.getCurrentValue(),
                            incident.getThresholdValue(),
                            incident.getStatus(),
                            incident.getPlaneIssueId(),
                            incident.getPlaneIssueUrl(),
                            incident.getAiTaskId(),
                            incident.getLastSeenAt() != null ? Timestamp.from(incident.getLastSeenAt()) : null,
                            incident.getResolvedAt() != null ? Timestamp.from(incident.getResolvedAt()) : null,
                            incident.getId()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to persist incident to DB, falling back to memory: {}", e.getMessage());
                if (incident.getId() == null) {
                    incident.setId(fallbackIdGenerator.incrementAndGet());
                }
            }
        } else {
            if (incident.getId() == null) {
                incident.setId(fallbackIdGenerator.incrementAndGet());
            }
        }

        memoryStore.put(key, incident);
        return incident;
    }

    public List<BookmakerIncident> findActiveIncidents() {
        if (dbAvailable) {
            try {
                String sql = "SELECT * FROM bookmaker_incidents WHERE status = 'OPEN' ORDER BY detected_at DESC";
                return jdbcTemplate.query(sql, ROW_MAPPER);
            } catch (Exception e) {
                log.warn("Failed to fetch active incidents from DB: {}", e.getMessage());
            }
        }
        return memoryStore.values().stream()
                .filter(i -> "OPEN".equalsIgnoreCase(i.getStatus()))
                .sorted(Comparator.comparing(BookmakerIncident::getDetectedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<BookmakerIncident> findAllIncidents(int limit) {
        if (dbAvailable) {
            try {
                String sql = "SELECT * FROM bookmaker_incidents ORDER BY detected_at DESC LIMIT ?";
                return jdbcTemplate.query(sql, ROW_MAPPER, limit);
            } catch (Exception e) {
                log.warn("Failed to fetch all incidents from DB: {}", e.getMessage());
            }
        }
        return memoryStore.values().stream()
                .sorted(Comparator.comparing(BookmakerIncident::getDetectedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    public Optional<BookmakerIncident> findById(Long id) {
        if (dbAvailable) {
            try {
                String sql = "SELECT * FROM bookmaker_incidents WHERE id = ?";
                List<BookmakerIncident> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                log.warn("Failed to query incident by id: {}", e.getMessage());
            }
        }
        return memoryStore.values().stream().filter(i -> Objects.equals(i.getId(), id)).findFirst();
    }

    public void resolveIncident(Long id) {
        Instant now = Instant.now();
        if (dbAvailable) {
            try {
                jdbcTemplate.update("UPDATE bookmaker_incidents SET status = 'RESOLVED', resolved_at = ? WHERE id = ?",
                        Timestamp.from(now), id);
            } catch (Exception e) {
                log.warn("Failed to resolve incident in DB: {}", e.getMessage());
            }
        }
        memoryStore.values().stream()
                .filter(i -> Objects.equals(i.getId(), id))
                .forEach(i -> {
                    i.setStatus("RESOLVED");
                    i.setResolvedAt(now);
                });
    }
}
