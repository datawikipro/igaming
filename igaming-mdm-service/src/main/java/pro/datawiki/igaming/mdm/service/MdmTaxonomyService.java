package pro.datawiki.igaming.mdm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.mdm.domain.*;
import pro.datawiki.igaming.mdm.dto.PlayerProfileDto;
import pro.datawiki.igaming.mdm.dto.PlayerProfileDto.ActiveMembershipDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto.SquadMemberDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto.TeamDnaDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto.TeamSofaMetadataDto;
import pro.datawiki.igaming.mdm.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MdmTaxonomyService {

    private final MdmSportRepository sportRepository;
    private final MdmCategoryRepository categoryRepository;
    private final MdmTournamentRepository tournamentRepository;
    private final MdmTeamRepository teamRepository;
    private final MdmTeamDnaRepository teamDnaRepository;
    private final MdmTeamSofaMetadataRepository teamSofaMetadataRepository;
    private final MdmTeamMembershipRepository teamMembershipRepository;
    private final MdmPlayerRepository playerRepository;
    private final MdmPlayerArchetypeRepository playerArchetypeRepository;

    @Transactional(readOnly = true)
    public List<MdmSport> getAllSports() {
        return sportRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MdmTournament> getTournamentsBySport(String sportSlug) {
        Optional<MdmSport> sportOpt = sportRepository.findBySlug(sportSlug.toLowerCase());
        if (sportOpt.isEmpty()) {
            return Collections.emptyList();
        }
        return tournamentRepository.findBySportIdAndIsFeaturedTrue(sportOpt.get().getId());
    }

    @Transactional(readOnly = true)
    public Optional<TeamProfileDto> getTeamProfile(Long teamId) {
        Optional<MdmTeam> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            return Optional.empty();
        }
        MdmTeam team = teamOpt.get();

        // 1. Team DNA
        TeamDnaDto dnaDto = teamDnaRepository.findById(teamId).map(dna -> TeamDnaDto.builder()
                .primaryFormation(dna.getPrimaryFormation())
                .playStyle(dna.getPlayStyle())
                .pressingIntensity(dna.getPressingIntensity())
                .possessionAvgPct(dna.getPossessionAvgPct())
                .aerialThreatScore(dna.getAerialThreatScore())
                .defenseVulnerabilityScore(dna.getDefenseVulnerabilityScore())
                .referenceTier(dna.getReferenceTier())
                .similarTeams(dna.getSimilarTeams())
                .build()).orElse(null);

        // 2. SofaScore Metadata
        TeamSofaMetadataDto sofaDto = teamSofaMetadataRepository.findById(teamId).map(meta -> TeamSofaMetadataDto.builder()
                .venueName(meta.getVenueName())
                .venueCity(meta.getVenueCity())
                .venueCapacity(meta.getVenueCapacity())
                .coachName(meta.getCoachName())
                .sofascoreRating(meta.getSofascoreRating())
                .formRecent(meta.getFormRecent())
                .build()).orElse(null);

        // 3. Active Squad Members
        List<MdmTeamMembership> memberships = teamMembershipRepository.findByTeamIdAndIsActiveTrue(teamId);
        List<SquadMemberDto> squad = memberships.stream().map(m -> {
            MdmPlayer p = m.getPlayer();
            Optional<MdmPlayerArchetype> archOpt = playerArchetypeRepository.findById(p.getId());

            return SquadMemberDto.builder()
                    .playerId(p.getId())
                    .playerName(p.getCommonName())
                    .playerSlug(p.getSlug())
                    .position(p.getPrimaryPosition())
                    .shirtNumber(m.getJerseyNumber())
                    .membershipType(m.getMembershipType())
                    .isJokerCandidate(archOpt.map(MdmPlayerArchetype::getIsJokerCandidate).orElse(false))
                    .overallRating(archOpt.map(MdmPlayerArchetype::getOverallRating).orElse(null))
                    .archetype(archOpt.map(MdmPlayerArchetype::getArchetype).orElse(null))
                    .similarPlayers(archOpt.map(MdmPlayerArchetype::getSimilarPlayers).orElse(null))
                    .build();
        }).collect(Collectors.toList());

        return Optional.of(TeamProfileDto.builder()
                .teamId(team.getId())
                .sportId(team.getSport().getId())
                .sportSlug(team.getSport().getSlug())
                .slug(team.getSlug())
                .canonicalName(team.getCanonicalName())
                .logoUrl(team.getLogoUrl())
                .gender(team.getGender())
                .ageCategory(team.getAgeCategory())
                .isNational(team.getIsNational())
                .isIndividual(team.getIsIndividual())
                .sofascoreId(team.getSofascoreId())
                .dna(dnaDto)
                .sofaMetadata(sofaDto)
                .squad(squad)
                .build());
    }

    @Transactional(readOnly = true)
    public Optional<PlayerProfileDto> getPlayerProfile(Long playerId) {
        Optional<MdmPlayer> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            return Optional.empty();
        }
        MdmPlayer player = playerOpt.get();
        Optional<MdmPlayerArchetype> archOpt = playerArchetypeRepository.findById(playerId);

        List<MdmTeamMembership> memberships = teamMembershipRepository.findByPlayerIdAndIsActiveTrue(playerId);
        List<ActiveMembershipDto> activeMemberships = memberships.stream().map(m -> ActiveMembershipDto.builder()
                .teamId(m.getTeam().getId())
                .teamName(m.getTeam().getCanonicalName())
                .teamSlug(m.getTeam().getSlug())
                .isNationalTeam(m.getTeam().getIsNational())
                .membershipType(m.getMembershipType())
                .shirtNumber(m.getJerseyNumber())
                .build()).collect(Collectors.toList());

        return Optional.of(PlayerProfileDto.builder()
                .playerId(player.getId())
                .canonicalName(player.getCommonName())
                .slug(player.getSlug())
                .position(player.getPrimaryPosition())
                .nationality(player.getCitizenshipCategory() != null ? player.getCitizenshipCategory().getCode() : null)
                .sofascoreId(player.getSofascoreId())
                .archetype(archOpt.map(MdmPlayerArchetype::getArchetype).orElse(null))
                .overallRating(archOpt.map(MdmPlayerArchetype::getOverallRating).orElse(null))
                .isJokerCandidate(archOpt.map(MdmPlayerArchetype::getIsJokerCandidate).orElse(false))
                .preferredFoot(archOpt.map(MdmPlayerArchetype::getPreferredFoot).orElse(null))
                .paceRating(archOpt.map(MdmPlayerArchetype::getPaceRating).orElse(null))
                .shootingRating(archOpt.map(MdmPlayerArchetype::getShootingRating).orElse(null))
                .passingRating(archOpt.map(MdmPlayerArchetype::getPassingRating).orElse(null))
                .referenceTier(archOpt.map(MdmPlayerArchetype::getReferenceTier).orElse(3))
                .similarPlayers(archOpt.map(MdmPlayerArchetype::getSimilarPlayers).orElse(null))
                .memberships(activeMemberships)
                .build());
    }
}
