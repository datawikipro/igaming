package pro.datawiki.igaming.mdm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.mdm.domain.*;
import pro.datawiki.igaming.mdm.dto.*;
import pro.datawiki.igaming.mdm.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MdmUnmappedQueueService {

    private final MdmUnmappedQueueRepository queueRepository;
    private final MdmExternalSystemRepository externalSystemRepository;
    private final MdmTournamentRepository tournamentRepository;
    private final MdmTeamRepository teamRepository;
    private final MdmTournamentTeamRepository tournamentTeamRepository;
    private final MdmTournamentIngestionMapRepository tournamentIngestionMapRepository;
    private final MdmTeamIngestionMapRepository teamIngestionMapRepository;
    private final MdmSportRepository sportRepository;
    private final MdmCountryRepository countryRepository;
    private final IngestionBlueprintService blueprintService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void enqueue(UnmappedAnomalyEvent event) {
        if (event == null || event.getExternalSystemCode() == null) {
            return;
        }

        String entityType = event.getEntityType();
        if (entityType != null) {
            entityType = entityType.toUpperCase(Locale.ROOT);
            if (entityType.contains("TOURNAMENT")) {
                entityType = "TOURNAMENT";
            } else if (entityType.contains("TEAM")) {
                entityType = "TEAM";
            }
        }
        if (!"TOURNAMENT".equals(entityType) && !"TEAM".equals(entityType)) {
            return;
        }

        String rawKey = event.getRawKey();
        if (rawKey == null || rawKey.isBlank() || "null".equalsIgnoreCase(rawKey.trim())) {
            if (event.getSourceId() != null && !event.getSourceId().isBlank()) {
                rawKey = event.getSourceId();
            } else if (event.getRawName() != null && !event.getRawName().isBlank()) {
                rawKey = event.getRawName();
            } else {
                return;
            }
        }
        rawKey = rawKey.trim();

        String rawName = event.getRawName() != null ? event.getRawName().trim() : null;
        String sourceId = event.getSourceId() != null ? event.getSourceId().trim() : null;

        try {
            queueRepository.upsertUnmapped(
                    event.getExternalSystemCode().trim().toLowerCase(Locale.ROOT),
                    entityType,
                    rawKey,
                    sourceId,
                    rawName,
                    event.getSportId(),
                    event.getSportSlug(),
                    event.getCountryId(),
                    event.getCountryName(),
                    event.getRawParentKey(),
                    event.getRawCategory()
            );
            log.info("📥 [MDM Queue] Enqueued unmapped entity: system={}, type={}, key='{}', sourceId='{}', name='{}'",
                    event.getExternalSystemCode(), entityType, rawKey, sourceId, rawName);
        } catch (Exception e) {
            log.error("Failed to enqueue unmapped entity {}: {}", rawKey, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<UnmappedQueueItemDto> getPendingItems(String entityType, String externalSystemCode, int pageNum, int limit) {
        int cappedLimit = Math.max(1, Math.min(limit, 200));
        PageRequest page = PageRequest.of(Math.max(0, pageNum), cappedLimit);

        List<MdmUnmappedQueue> items;
        if (entityType != null && !entityType.isBlank() && externalSystemCode != null && !externalSystemCode.isBlank()) {
            items = queueRepository.findByStatusAndEntityTypeAndExternalSystemCodeOrderByCreatedAtAsc(
                    "PENDING", entityType.toUpperCase(Locale.ROOT), externalSystemCode.toLowerCase(Locale.ROOT), page);
        } else if (entityType != null && !entityType.isBlank()) {
            items = queueRepository.findByStatusAndEntityTypeOrderByCreatedAtAsc(
                    "PENDING", entityType.toUpperCase(Locale.ROOT), page);
        } else if (externalSystemCode != null && !externalSystemCode.isBlank()) {
            items = queueRepository.findByStatusAndExternalSystemCodeOrderByCreatedAtAsc(
                    "PENDING", externalSystemCode.toLowerCase(Locale.ROOT), page);
        } else {
            items = queueRepository.findByStatusOrderByCreatedAtAsc("PENDING", page);
        }

        return items.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public boolean ignoreItem(Long id, String reason) {
        if (id == null) return false;
        int updated = queueRepository.updateStatus(id, "IGNORED", reason != null ? reason : "Ignored by LLM Worker");
        if (updated > 0) {
            log.info("⏩ [MDM Queue] Ignored unmapped item #{} (reason: {})", id, reason);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<TournamentCandidateDto> getTournamentCandidates(Integer sportId, String sportSlug, String country, String search) {
        Integer resolvedSportId = sportId;
        if (resolvedSportId == null && sportSlug != null && !sportSlug.isBlank()) {
            resolvedSportId = sportRepository.findBySlug(sportSlug.trim().toLowerCase(Locale.ROOT))
                    .map(MdmSport::getId)
                    .orElse(null);
        }
        if (resolvedSportId == null) {
            resolvedSportId = 1; // Default to Football
        }

        String cSlug = null;
        if (country != null && !country.isBlank()) {
            pro.datawiki.igaming.mdm.util.CountryNormalizer.CountryInfo cInfo = pro.datawiki.igaming.mdm.util.CountryNormalizer.normalize(country, false);
            cSlug = (cInfo != null && cInfo.slug() != null) ? cInfo.slug().toLowerCase(Locale.ROOT) : country.trim().toLowerCase(Locale.ROOT);
        }

        String searchTrim = (search != null && !search.isBlank()) ? search.trim() : null;

        List<MdmTournament> list = tournamentRepository.searchTournaments(
                resolvedSportId,
                cSlug,
                searchTrim,
                PageRequest.of(0, 30)
        );

        if (list.isEmpty() && searchTrim != null && cSlug != null) {
            list = tournamentRepository.searchTournaments(resolvedSportId, null, searchTrim, PageRequest.of(0, 30));
        }

        return list.stream()
                .map(t -> TournamentCandidateDto.builder()
                        .tournamentId(t.getId())
                        .name(t.getCanonicalName())
                        .sportId(t.getSport() != null ? t.getSport().getId() : null)
                        .countryId(t.getCountry() != null ? t.getCountry().getId() : null)
                        .countryName(t.getCountryName())
                        .slug(t.getSlug())
                        .gender(t.getGender())
                        .ageCategory(t.getAgeCategory())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamCandidateDto> getTeamCandidates(Long tournamentId, Integer sportId, Integer countryId, String search) {
        List<MdmTeam> list = Collections.emptyList();

        if (tournamentId != null) {
            list = tournamentTeamRepository.findActiveTeamsByTournamentId(tournamentId);
            if (list.isEmpty()) {
                List<Long> matchTeamIds = jdbcTemplate.queryForList(
                        "SELECT DISTINCT team_id FROM (" +
                        "  SELECT home_team_id AS team_id FROM mdm_match WHERE tournament_id = ? " +
                        "  UNION " +
                        "  SELECT away_team_id AS team_id FROM mdm_match WHERE tournament_id = ? " +
                        ") sub LIMIT 100",
                        Long.class, tournamentId, tournamentId
                );
                if (!matchTeamIds.isEmpty()) {
                    list = teamRepository.findAllById(matchTeamIds);
                }
            }
        }

        if (list.isEmpty() && search != null && !search.isBlank()) {
            list = teamRepository.searchTeams(sportId, search.trim(), PageRequest.of(0, 30));
        }

        if (list.isEmpty() && sportId != null && countryId != null) {
            list = teamRepository.findBySportIdAndCountryId(sportId, countryId);
        }

        if (tournamentId != null && search != null && !search.isBlank()) {
            final String sNorm = search.trim().toLowerCase(Locale.ROOT);
            list = list.stream()
                    .filter(t -> (t.getCanonicalName() != null && t.getCanonicalName().toLowerCase(Locale.ROOT).contains(sNorm))
                              || (t.getSlug() != null && t.getSlug().toLowerCase(Locale.ROOT).contains(sNorm)))
                    .collect(Collectors.toList());
        }

        return list.stream()
                .limit(50)
                .map(t -> TeamCandidateDto.builder()
                        .teamId(t.getId())
                        .name(t.getCanonicalName())
                        .sportId(t.getSport() != null ? t.getSport().getId() : null)
                        .countryId(t.getCountry() != null ? t.getCountry().getId() : null)
                        .countryName(t.getCountryName())
                        .slug(t.getSlug())
                        .gender(t.getGender())
                        .ageCategory(t.getAgeCategory())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean resolveTournament(ResolveTournamentMappingRequest request) {
        if (request == null || request.getExternalSystemCode() == null || request.getRawKey() == null || request.getCanonicalTournamentId() == null) {
            throw new IllegalArgumentException("externalSystemCode, rawKey, and canonicalTournamentId are required");
        }

        String sysCode = request.getExternalSystemCode().trim().toLowerCase(Locale.ROOT);
        MdmExternalSystem system = externalSystemRepository.findByCode(sysCode)
                .orElseThrow(() -> new IllegalArgumentException("External system not found: " + sysCode));

        MdmTournament canonicalTourn = tournamentRepository.findById(request.getCanonicalTournamentId())
                .orElseThrow(() -> new IllegalArgumentException("Canonical tournament not found: " + request.getCanonicalTournamentId()));

        String rawKey = request.getRawKey().trim();
        String sourceId = (request.getSourceId() != null && !request.getSourceId().isBlank()) ? request.getSourceId().trim() : null;
        String rawName = request.getRawName() != null ? request.getRawName().trim() : canonicalTourn.getCanonicalName();

        // 1. Upsert ingestion map
        tournamentIngestionMapRepository.upsertMapping(
                system.getId(),
                rawKey,
                sourceId,
                canonicalTourn.getId(),
                rawName
        );

        // 2. Mark queue item resolved
        String notes = request.getResolutionNotes() != null ? request.getResolutionNotes() : "Resolved by LLM Agent Worker";
        if (request.getQueueItemId() != null) {
            queueRepository.updateStatus(request.getQueueItemId(), "RESOLVED", notes);
        } else {
            queueRepository.findByExternalSystemCodeAndEntityTypeAndRawKey(sysCode, "TOURNAMENT", rawKey)
                    .ifPresent(q -> queueRepository.updateStatus(q.getId(), "RESOLVED", notes));
        }

        // 3. Propagate canonical tournament ID as context_parent_key to pending teams
        try {
            String tournName = request.getRawName() != null ? request.getRawName() : rawKey;
            jdbcTemplate.update(
                    "UPDATE mdm_unmapped_queue SET context_parent_key = ? WHERE entity_type = 'TEAM' AND external_system_code = ? AND context_parent_name = ?",
                    String.valueOf(canonicalTourn.getId()), sysCode, tournName
            );
        } catch (Exception e) {
            log.warn("Failed to propagate context_parent_key: {}", e.getMessage());
        }

        // 5. Invalidate Blueprint cache
        blueprintService.invalidate(sysCode);
        log.info("✅ [MDM Queue] Resolved TOURNAMENT mapping: system={}, rawKey='{}', sourceId='{}' -> tournament #{} '{}'",
                sysCode, rawKey, sourceId, canonicalTourn.getId(), canonicalTourn.getCanonicalName());

        return true;
    }

    @Transactional
    public boolean resolveTeam(ResolveTeamMappingRequest request) {
        if (request == null || request.getExternalSystemCode() == null || request.getRawKey() == null || request.getCanonicalTeamId() == null) {
            throw new IllegalArgumentException("externalSystemCode, rawKey, and canonicalTeamId are required");
        }

        String sysCode = request.getExternalSystemCode().trim().toLowerCase(Locale.ROOT);
        MdmExternalSystem system = externalSystemRepository.findByCode(sysCode)
                .orElseThrow(() -> new IllegalArgumentException("External system not found: " + sysCode));

        MdmTeam canonicalTeam = teamRepository.findById(request.getCanonicalTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Canonical team not found: " + request.getCanonicalTeamId()));

        String rawKey = request.getRawKey().trim();
        String sourceId = (request.getSourceId() != null && !request.getSourceId().isBlank()) ? request.getSourceId().trim() : null;
        String rawName = request.getRawName() != null ? request.getRawName().trim() : canonicalTeam.getCanonicalName();

        // 1. Upsert ingestion map
        teamIngestionMapRepository.upsertMapping(
                system.getId(),
                rawKey,
                sourceId,
                canonicalTeam.getId(),
                rawName
        );

        // 2. Link team to tournament if provided
        if (request.getTournamentId() != null) {
            tournamentRepository.findById(request.getTournamentId()).ifPresent(tourn -> {
                if (!tournamentTeamRepository.existsByTournamentIdAndTeamId(tourn.getId(), canonicalTeam.getId())) {
                    MdmTournamentTeam tt = MdmTournamentTeam.builder()
                            .tournament(tourn)
                            .team(canonicalTeam)
                            .isActive(true)
                            .build();
                    tournamentTeamRepository.save(tt);
                    log.info("Linked team #{} '{}' to tournament #{} '{}'", canonicalTeam.getId(), canonicalTeam.getCanonicalName(), tourn.getId(), tourn.getCanonicalName());
                }
            });
        }

        // 3. Mark queue item resolved
        String notes = request.getResolutionNotes() != null ? request.getResolutionNotes() : "Resolved by LLM Agent Worker";
        if (request.getQueueItemId() != null) {
            queueRepository.updateStatus(request.getQueueItemId(), "RESOLVED", notes);
        } else {
            queueRepository.findByExternalSystemCodeAndEntityTypeAndRawKey(sysCode, "TEAM", rawKey)
                    .ifPresent(q -> queueRepository.updateStatus(q.getId(), "RESOLVED", notes));
        }

        // 4. Invalidate Blueprint cache
        blueprintService.invalidate(sysCode);
        log.info("✅ [MDM Queue] Resolved TEAM mapping: system={}, rawKey='{}', sourceId='{}' -> team #{} '{}'",
                sysCode, rawKey, sourceId, canonicalTeam.getId(), canonicalTeam.getCanonicalName());

        return true;
    }

    @Transactional
    public int syncUnmappedFromBookmakers() {
        // Find recent bookmaker matches created without SofaScore link
        String sql = """
            SELECT m.source_origin, m.created_by_source, s.slug as sport_slug, s.id as sport_id,
                   t.canonical_name as tourn_name, t.country_name,
                   ht.canonical_name as home_name, at.canonical_name as away_name
            FROM mdm_match m
            JOIN mdm_sport s ON m.sport_id = s.id
            JOIN mdm_tournament t ON m.tournament_id = t.id
            JOIN mdm_team ht ON m.home_team_id = ht.id
            JOIN mdm_team at ON m.away_team_id = at.id
            WHERE m.source_origin = 'BOOKMAKER'
              AND m.start_time BETWEEN NOW() - INTERVAL '24 hours' AND NOW() + INTERVAL '72 hours'
            ORDER BY m.start_time ASC
            LIMIT 3000
            """;

        int count = 0;
        List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (java.util.Map<String, Object> r : rows) {
            String source = (String) r.get("created_by_source");
            if (source == null || source.isBlank()) source = "melbet";
            String sportSlug = (String) r.get("sport_slug");
            Integer sportId = (Integer) r.get("sport_id");
            String country = (String) r.get("country_name");

            String tournName = (String) r.get("tourn_name");
            if (tournName != null && !tournName.isBlank()) {
                enqueue(UnmappedAnomalyEvent.builder()
                        .externalSystemCode(source)
                        .entityType("TOURNAMENT")
                        .rawKey(tournName)
                        .rawName(tournName)
                        .sportId(sportId)
                        .sportSlug(sportSlug)
                        .countryName(country)
                        .build());
                count++;
            }

            String homeName = (String) r.get("home_name");
            if (homeName != null && !homeName.isBlank()) {
                enqueue(UnmappedAnomalyEvent.builder()
                        .externalSystemCode(source)
                        .entityType("TEAM")
                        .rawKey(homeName)
                        .rawName(homeName)
                        .rawCategory(tournName)
                        .sportId(sportId)
                        .sportSlug(sportSlug)
                        .countryName(country)
                        .build());
                count++;
            }

            String awayName = (String) r.get("away_name");
            if (awayName != null && !awayName.isBlank()) {
                enqueue(UnmappedAnomalyEvent.builder()
                        .externalSystemCode(source)
                        .entityType("TEAM")
                        .rawKey(awayName)
                        .rawName(awayName)
                        .rawCategory(tournName)
                        .sportId(sportId)
                        .sportSlug(sportSlug)
                        .countryName(country)
                        .build());
                count++;
            }
        }
        return count;
    }

    private UnmappedQueueItemDto toDto(MdmUnmappedQueue q) {
        return UnmappedQueueItemDto.builder()
                .id(q.getId())
                .externalSystemCode(q.getExternalSystemCode())
                .entityType(q.getEntityType())
                .rawKey(q.getRawKey())
                .sourceId(q.getSourceId())
                .rawName(q.getRawName())
                .sportId(q.getSportId())
                .sportSlug(q.getSportSlug())
                .countryId(q.getCountryId())
                .countryName(q.getCountryName())
                .contextParentKey(q.getContextParentKey())
                .contextParentName(q.getContextParentName())
                .status(q.getStatus())
                .resolutionNotes(q.getResolutionNotes())
                .createdAt(q.getCreatedAt())
                .build();
    }
}
