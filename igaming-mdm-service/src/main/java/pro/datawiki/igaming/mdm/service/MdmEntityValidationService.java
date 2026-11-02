package pro.datawiki.igaming.mdm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.mdm.domain.*;
import pro.datawiki.igaming.dto.UnmappedAnomalyEvent;
import pro.datawiki.igaming.mdm.repository.*;

import java.util.Optional;

/**
 * Stage 1 Validation Guard: Strict SofaScore Mandate.
 * No synthetic matches or entities are created without a verified SofaScore ID.
 * Unmapped entities are immediately reported to the error collection service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MdmEntityValidationService {

    private final MdmSportRepository sportRepository;
    private final MdmTournamentRepository tournamentRepository;
    private final MdmTeamRepository teamRepository;
    private final AnomalyReportingService anomalyReportingService;

    /**
     * Resolves canonical match components strictly against the SofaScore Golden Record.
     * If any component lacks a verified SofaScore ID, returns empty and emits an anomaly error.
     */
    @Transactional(readOnly = true)
    public Optional<ResolvedMatchIdentity> resolveStrictStage1Match(
            String externalSystemCode,
            String rawSportKey,
            String rawTournamentKey,
            String rawTournamentName,
            String rawHomeTeamKey,
            String rawHomeTeamName,
            String rawAwayTeamKey,
            String rawAwayTeamName
    ) {
        // 1. Resolve Tournament
        Optional<MdmTournament> tournamentOpt = tournamentRepository.findBySofascoreId(tryParseLong(rawTournamentKey));
        if (tournamentOpt.isEmpty() || tournamentOpt.get().getSofascoreId() == null) {
            anomalyReportingService.reportAnomaly(UnmappedAnomalyEvent.builder()
                    .externalSystemCode(externalSystemCode)
                    .entityType("TOURNAMENT")
                    .rawKey(rawTournamentKey)
                    .rawName(rawTournamentName)
                    .details("Stage-1 Strict Guard: Tournament missing in SofaScore Golden Record")
                    .build());
            return Optional.empty();
        }

        MdmTournament tournament = tournamentOpt.get();

        // 2. Resolve Home Team
        Optional<MdmTeam> homeTeamOpt = teamRepository.findBySofascoreId(tryParseLong(rawHomeTeamKey));
        if (homeTeamOpt.isEmpty() || homeTeamOpt.get().getSofascoreId() == null) {
            anomalyReportingService.reportAnomaly(UnmappedAnomalyEvent.builder()
                    .externalSystemCode(externalSystemCode)
                    .entityType("TEAM")
                    .rawKey(rawHomeTeamKey)
                    .rawName(rawHomeTeamName)
                    .rawParentKey(rawTournamentKey)
                    .details("Stage-1 Strict Guard: Home team missing in SofaScore Golden Record")
                    .build());
            return Optional.empty();
        }

        // 3. Resolve Away Team
        Optional<MdmTeam> awayTeamOpt = teamRepository.findBySofascoreId(tryParseLong(rawAwayTeamKey));
        if (awayTeamOpt.isEmpty() || awayTeamOpt.get().getSofascoreId() == null) {
            anomalyReportingService.reportAnomaly(UnmappedAnomalyEvent.builder()
                    .externalSystemCode(externalSystemCode)
                    .entityType("TEAM")
                    .rawKey(rawAwayTeamKey)
                    .rawName(rawAwayTeamName)
                    .rawParentKey(rawTournamentKey)
                    .details("Stage-1 Strict Guard: Away team missing in SofaScore Golden Record")
                    .build());
            return Optional.empty();
        }

        return Optional.of(new ResolvedMatchIdentity(
                tournament.getSport().getId(),
                tournament.getId(),
                homeTeamOpt.get().getId(),
                awayTeamOpt.get().getId(),
                tournament.getSofascoreId(),
                homeTeamOpt.get().getSofascoreId(),
                awayTeamOpt.get().getSofascoreId()
        ));
    }

    private Long tryParseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public record ResolvedMatchIdentity(
            Integer sportId,
            Long tournamentId,
            Long homeTeamId,
            Long awayTeamId,
            Long tournamentSofascoreId,
            Long homeTeamSofascoreId,
            Long awayTeamSofascoreId
    ) {}
}
