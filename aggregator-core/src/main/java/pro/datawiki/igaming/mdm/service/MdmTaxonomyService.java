/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package pro.datawiki.igaming.mdm.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.mdm.domain.MdmPlayer;
import pro.datawiki.igaming.mdm.domain.MdmPlayerArchetype;
import pro.datawiki.igaming.mdm.domain.MdmSport;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTeamMembership;
import pro.datawiki.igaming.mdm.domain.MdmTournament;
import pro.datawiki.igaming.mdm.dto.PlayerProfileDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto;
import pro.datawiki.igaming.mdm.repository.MdmCategoryRepository;
import pro.datawiki.igaming.mdm.repository.MdmPlayerArchetypeRepository;
import pro.datawiki.igaming.mdm.repository.MdmPlayerRepository;
import pro.datawiki.igaming.mdm.repository.MdmSportRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamDnaRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamMembershipRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamSofaMetadataRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentRepository;

@Service
public class MdmTaxonomyService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(MdmTaxonomyService.class);
    private final MdmSportRepository sportRepository;
    private final MdmCategoryRepository categoryRepository;
    private final MdmTournamentRepository tournamentRepository;
    private final MdmTeamRepository teamRepository;
    private final MdmTeamDnaRepository teamDnaRepository;
    private final MdmTeamSofaMetadataRepository teamSofaMetadataRepository;
    private final MdmTeamMembershipRepository teamMembershipRepository;
    private final MdmPlayerRepository playerRepository;
    private final MdmPlayerArchetypeRepository playerArchetypeRepository;

    @Transactional(readOnly=true)
    public List<MdmSport> getAllSports() {
        return this.sportRepository.findAll();
    }

    @Transactional(readOnly=true)
    public List<MdmTournament> getTournamentsBySport(String sportSlug) {
        Optional<MdmSport> sportOpt = this.sportRepository.findBySlug(sportSlug.toLowerCase());
        if (sportOpt.isEmpty()) {
            return Collections.emptyList();
        }
        return this.tournamentRepository.findBySportIdAndIsFeaturedTrue(sportOpt.get().getId());
    }

    @Transactional(readOnly=true)
    public Optional<TeamProfileDto> getTeamProfile(Long teamId) {
        Optional teamOpt = this.teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            return Optional.empty();
        }
        MdmTeam team = (MdmTeam)teamOpt.get();
        TeamProfileDto.TeamDnaDto dnaDto = this.teamDnaRepository.findById(teamId).map(dna -> TeamProfileDto.TeamDnaDto.builder().primaryFormation(dna.getPrimaryFormation()).playStyle(dna.getPlayStyle()).pressingIntensity(dna.getPressingIntensity()).possessionAvgPct(dna.getPossessionAvgPct()).aerialThreatScore(dna.getAerialThreatScore()).defenseVulnerabilityScore(dna.getDefenseVulnerabilityScore()).referenceTier(dna.getReferenceTier()).similarTeams(dna.getSimilarTeams()).build()).orElse(null);
        TeamProfileDto.TeamSofaMetadataDto sofaDto = this.teamSofaMetadataRepository.findById(teamId).map(meta -> TeamProfileDto.TeamSofaMetadataDto.builder().venueName(meta.getVenueName()).venueCity(meta.getVenueCity()).venueCapacity(meta.getVenueCapacity()).coachName(meta.getCoachName()).sofascoreRating(meta.getSofascoreRating()).formRecent(meta.getFormRecent()).build()).orElse(null);
        List<MdmTeamMembership> memberships = this.teamMembershipRepository.findByTeamIdAndIsActiveTrue(teamId);
        List<TeamProfileDto.SquadMemberDto> squad = memberships.stream().map(m -> {
            MdmPlayer p = m.getPlayer();
            Optional<MdmPlayerArchetype> archOpt = this.playerArchetypeRepository.findById(p.getId());
            return TeamProfileDto.SquadMemberDto.builder().playerId(p.getId()).playerName(p.getCommonName()).playerSlug(p.getSlug()).position(p.getPrimaryPosition()).shirtNumber(m.getJerseyNumber()).membershipType(m.getMembershipType()).isJokerCandidate(archOpt.map(MdmPlayerArchetype::getIsJokerCandidate).orElse(false)).overallRating(archOpt.map(MdmPlayerArchetype::getOverallRating).orElse(null)).archetype(archOpt.map(MdmPlayerArchetype::getArchetype).orElse(null)).similarPlayers(archOpt.map(MdmPlayerArchetype::getSimilarPlayers).orElse(null)).build();
        }).collect(Collectors.toList());
        return Optional.of(TeamProfileDto.builder().teamId(team.getId()).sportId(team.getSport().getId()).sportSlug(team.getSport().getSlug()).slug(team.getSlug()).canonicalName(team.getCanonicalName()).logoUrl(team.getLogoUrl()).gender(team.getGender()).ageCategory(team.getAgeCategory()).isNational(team.getIsNational()).isIndividual(team.getIsIndividual()).sofascoreId(team.getSofascoreId()).dna(dnaDto).sofaMetadata(sofaDto).squad(squad).build());
    }

    @Transactional(readOnly=true)
    public Optional<PlayerProfileDto> getPlayerProfile(Long playerId) {
        Optional playerOpt = this.playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            return Optional.empty();
        }
        MdmPlayer player = (MdmPlayer)playerOpt.get();
        Optional<MdmPlayerArchetype> archOpt = this.playerArchetypeRepository.findById(playerId);
        List<MdmTeamMembership> memberships = this.teamMembershipRepository.findByPlayerIdAndIsActiveTrue(playerId);
        List<PlayerProfileDto.ActiveMembershipDto> activeMemberships = memberships.stream().map(m -> PlayerProfileDto.ActiveMembershipDto.builder().teamId(m.getTeam().getId()).teamName(m.getTeam().getCanonicalName()).teamSlug(m.getTeam().getSlug()).isNationalTeam(m.getTeam().getIsNational()).membershipType(m.getMembershipType()).shirtNumber(m.getJerseyNumber()).build()).collect(Collectors.toList());
        return Optional.of(PlayerProfileDto.builder().playerId(player.getId()).canonicalName(player.getCommonName()).slug(player.getSlug()).position(player.getPrimaryPosition()).nationality(player.getCitizenshipCategory() != null ? player.getCitizenshipCategory().getCode() : null).sofascoreId(player.getSofascoreId()).archetype(archOpt.map(MdmPlayerArchetype::getArchetype).orElse(null)).overallRating(archOpt.map(MdmPlayerArchetype::getOverallRating).orElse(null)).isJokerCandidate(archOpt.map(MdmPlayerArchetype::getIsJokerCandidate).orElse(false)).preferredFoot(archOpt.map(MdmPlayerArchetype::getPreferredFoot).orElse(null)).paceRating(archOpt.map(MdmPlayerArchetype::getPaceRating).orElse(null)).shootingRating(archOpt.map(MdmPlayerArchetype::getShootingRating).orElse(null)).passingRating(archOpt.map(MdmPlayerArchetype::getPassingRating).orElse(null)).referenceTier(archOpt.map(MdmPlayerArchetype::getReferenceTier).orElse(3)).similarPlayers(archOpt.map(MdmPlayerArchetype::getSimilarPlayers).orElse(null)).memberships(activeMemberships).build());
    }

    @Generated
    public MdmTaxonomyService(MdmSportRepository sportRepository, MdmCategoryRepository categoryRepository, MdmTournamentRepository tournamentRepository, MdmTeamRepository teamRepository, MdmTeamDnaRepository teamDnaRepository, MdmTeamSofaMetadataRepository teamSofaMetadataRepository, MdmTeamMembershipRepository teamMembershipRepository, MdmPlayerRepository playerRepository, MdmPlayerArchetypeRepository playerArchetypeRepository) {
        this.sportRepository = sportRepository;
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.teamDnaRepository = teamDnaRepository;
        this.teamSofaMetadataRepository = teamSofaMetadataRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.playerRepository = playerRepository;
        this.playerArchetypeRepository = playerArchetypeRepository;
    }
}

