/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 *  pro.datawiki.igaming.dto.UnmappedAnomalyEvent
 */
package pro.datawiki.igaming.mdm.service;

import java.util.Optional;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.UnmappedAnomalyEvent;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTournament;
import pro.datawiki.igaming.mdm.repository.MdmSportRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentRepository;
import pro.datawiki.igaming.mdm.service.AnomalyReportingService;

@Service
public class MdmEntityValidationService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(MdmEntityValidationService.class);
    private final MdmSportRepository sportRepository;
    private final MdmTournamentRepository tournamentRepository;
    private final MdmTeamRepository teamRepository;
    private final AnomalyReportingService anomalyReportingService;

    @Transactional(readOnly=true)
    public Optional<ResolvedMatchIdentity> resolveStrictStage1Match(String externalSystemCode, String rawSportKey, String rawTournamentKey, String rawTournamentName, String rawHomeTeamKey, String rawHomeTeamName, String rawAwayTeamKey, String rawAwayTeamName) {
        Optional<MdmTournament> tournamentOpt = this.tournamentRepository.findBySofascoreId(this.tryParseLong(rawTournamentKey));
        if (tournamentOpt.isEmpty() || tournamentOpt.get().getSofascoreId() == null) {
            this.anomalyReportingService.reportAnomaly(UnmappedAnomalyEvent.builder().externalSystemCode(externalSystemCode).entityType("TOURNAMENT").rawKey(rawTournamentKey).rawName(rawTournamentName).details("Stage-1 Strict Guard: Tournament missing in SofaScore Golden Record").build());
            return Optional.empty();
        }
        MdmTournament tournament = tournamentOpt.get();
        Optional<MdmTeam> homeTeamOpt = this.teamRepository.findBySofascoreId(this.tryParseLong(rawHomeTeamKey));
        if (homeTeamOpt.isEmpty() || homeTeamOpt.get().getSofascoreId() == null) {
            this.anomalyReportingService.reportAnomaly(UnmappedAnomalyEvent.builder().externalSystemCode(externalSystemCode).entityType("TEAM").rawKey(rawHomeTeamKey).rawName(rawHomeTeamName).rawParentKey(rawTournamentKey).details("Stage-1 Strict Guard: Home team missing in SofaScore Golden Record").build());
            return Optional.empty();
        }
        Optional<MdmTeam> awayTeamOpt = this.teamRepository.findBySofascoreId(this.tryParseLong(rawAwayTeamKey));
        if (awayTeamOpt.isEmpty() || awayTeamOpt.get().getSofascoreId() == null) {
            this.anomalyReportingService.reportAnomaly(UnmappedAnomalyEvent.builder().externalSystemCode(externalSystemCode).entityType("TEAM").rawKey(rawAwayTeamKey).rawName(rawAwayTeamName).rawParentKey(rawTournamentKey).details("Stage-1 Strict Guard: Away team missing in SofaScore Golden Record").build());
            return Optional.empty();
        }
        return Optional.of(new ResolvedMatchIdentity(tournament.getSport().getId(), tournament.getId(), homeTeamOpt.get().getId(), awayTeamOpt.get().getId(), tournament.getSofascoreId(), homeTeamOpt.get().getSofascoreId(), awayTeamOpt.get().getSofascoreId()));
    }

    private Long tryParseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    @Generated
    public MdmEntityValidationService(MdmSportRepository sportRepository, MdmTournamentRepository tournamentRepository, MdmTeamRepository teamRepository, AnomalyReportingService anomalyReportingService) {
        this.sportRepository = sportRepository;
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.anomalyReportingService = anomalyReportingService;
    }

    public record ResolvedMatchIdentity(Integer sportId, Long tournamentId, Long homeTeamId, Long awayTeamId, Long tournamentSofascoreId, Long homeTeamSofascoreId, Long awayTeamSofascoreId) {
    }
}

