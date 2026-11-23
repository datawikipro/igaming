package pro.datawiki.igaming.mdm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.dto.athlete.AthleteCardDto;
import pro.datawiki.igaming.dto.athlete.AthleteInjuryDto;
import pro.datawiki.igaming.dto.athlete.AthleteMembershipDto;
import pro.datawiki.igaming.dto.athlete.AthleteWorkloadDto;
import pro.datawiki.igaming.mdm.domain.*;
import pro.datawiki.igaming.mdm.dto.PlayerProfileDto;
import pro.datawiki.igaming.mdm.dto.PlayerProfileDto.ActiveMembershipDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto.SquadMemberDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto.TeamDnaDto;
import pro.datawiki.igaming.mdm.dto.TeamProfileDto.TeamSofaMetadataDto;
import pro.datawiki.igaming.mdm.repository.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final PlayerWorkloadService playerWorkloadService;
    private final MdmPlayerInjuryLogRepository injuryLogRepository;

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
                    .photoUrl(p.getPhotoUrl())
                    .photoSource(p.getPhotoSource())
                    .isOfficialPhoto(p.getIsOfficialPhoto())
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
                .logoSource(team.getLogoSource())
                .isOfficialLogo(team.getIsOfficialLogo())
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

        List<MdmTeamMembership> memberships = teamMembershipRepository.findByPlayerId(playerId);
        List<ActiveMembershipDto> activeMemberships = memberships.stream().map(m -> ActiveMembershipDto.builder()
                .teamId(m.getTeam().getId())
                .teamName(m.getTeam().getCanonicalName())
                .teamSlug(m.getTeam().getSlug())
                .isNationalTeam(m.getTeam().getIsNational())
                .membershipType(m.getMembershipType())
                .shirtNumber(m.getJerseyNumber())
                .firstMatchAt(m.getFirstMatchAt())
                .lastMatchAt(m.getLastMatchAt())
                .totalMatches(m.getTotalMatches())
                .isActive(m.getIsActive())
                .build()).collect(Collectors.toList());

        return Optional.of(PlayerProfileDto.builder()
                .playerId(player.getId())
                .canonicalName(player.getCommonName())
                .slug(player.getSlug())
                .position(player.getPrimaryPosition())
                .nationality(player.getCitizenshipCategory() != null ? player.getCitizenshipCategory().getCode() : null)
                .sofascoreId(player.getSofascoreId())
                .photoUrl(player.getPhotoUrl())
                .photoSource(player.getPhotoSource())
                .isOfficialPhoto(player.getIsOfficialPhoto())
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

    @Transactional(readOnly = true)
    public Optional<AthleteCardDto> getAthleteCard(Long playerId) {
        return playerRepository.findById(playerId).map(this::buildAthleteCard);
    }

    @Transactional(readOnly = true)
    public Optional<AthleteCardDto> getAthleteCardBySofascoreId(Long sofascoreId) {
        return playerRepository.findBySofascoreId(sofascoreId).map(this::buildAthleteCard);
    }

    private AthleteCardDto buildAthleteCard(MdmPlayer player) {
        Long playerId = player.getId();

        // 1. Memberships
        List<MdmTeamMembership> memberships = teamMembershipRepository.findByPlayerId(playerId);
        List<AthleteMembershipDto> membershipDtos = memberships.stream().map(m -> AthleteMembershipDto.builder()
                .teamId(m.getTeam().getId())
                .teamName(m.getTeam().getCanonicalName())
                .teamSlug(m.getTeam().getSlug())
                .isNationalTeam(m.getTeam().getIsNational())
                .membershipType(m.getMembershipType())
                .jerseyNumber(m.getJerseyNumber())
                .squadRole(m.getSquadRole())
                .isActive(m.getIsActive())
                .firstMatchAt(m.getFirstMatchAt())
                .lastMatchAt(m.getLastMatchAt())
                .totalMatches(m.getTotalMatches())
                .build()).collect(Collectors.toList());

        // 2. Workload & Fatigue
        AthleteWorkloadDto workload = playerWorkloadService.calculateWorkload(playerId);

        // 3. Injuries
        List<MdmPlayerInjuryLog> injuryLogs = injuryLogRepository.findByPlayerIdOrderByStartedAtDesc(playerId);
        List<AthleteInjuryDto> activeInjuries = injuryLogs.stream()
                .filter(i -> i.getActualReturnedAt() == null)
                .map(this::toInjuryDto)
                .collect(Collectors.toList());
        List<AthleteInjuryDto> injuryHistory = injuryLogs.stream()
                .filter(i -> i.getActualReturnedAt() != null)
                .map(this::toInjuryDto)
                .collect(Collectors.toList());

        // 4. Country & Flag
        String nationality = null;
        String nationalityCode = null;
        String flagEmoji = null;
        if (player.getNationality() != null) {
            nationality = player.getNationality().getCanonicalName();
            nationalityCode = player.getNationality().getIsoCode();
            flagEmoji = player.getNationality().getFlagCode() != null 
                    ? player.getNationality().getFlagCode() 
                    : toFlagEmoji(nationalityCode);
        } else if (player.getCitizenshipCategory() != null) {
            nationality = player.getCitizenshipCategory().getSlug();
            nationalityCode = player.getCitizenshipCategory().getCode();
            flagEmoji = toFlagEmoji(nationalityCode);
        }

        // 5. Age
        Integer age = null;
        if (player.getBirthDate() != null) {
            age = Period.between(player.getBirthDate(), LocalDate.now()).getYears();
        }

        // 6. Sport Attributes map
        Map<String, Object> sportAttrs = player.getSportAttributes() != null 
                ? player.getSportAttributes() 
                : Collections.emptyMap();

        String sportCode = player.getSport() != null ? player.getSport().getSlug().toUpperCase() : "FOOTBALL";

        return AthleteCardDto.builder()
                .playerId(player.getId())
                .externalRefId(player.getSofascoreId())
                .commonName(player.getCommonName())
                .firstName(player.getFirstName())
                .lastName(player.getLastName())
                .slug(player.getSlug())
                .birthDate(player.getBirthDate())
                .age(age)
                .heightCm(player.getHeightCm())
                .weightKg(player.getWeightKg())
                .nationality(nationality)
                .nationalityCode(nationalityCode)
                .nationalityFlag(flagEmoji)
                .gender(player.getGender())
                .photoUrl(player.getPhotoUrl())
                .photoSource(player.getPhotoSource())
                .isOfficialPhoto(player.getIsOfficialPhoto())
                .sportCode(sportCode)
                .primaryPosition(player.getPrimaryPosition())
                .healthStatus(player.getHealthStatus() != null ? player.getHealthStatus() : "HEALTHY")
                .sportAttributes(sportAttrs)
                .memberships(membershipDtos)
                .workload(workload)
                .activeInjuries(activeInjuries)
                .injuryHistory(injuryHistory)
                .build();
    }

    private AthleteInjuryDto toInjuryDto(MdmPlayerInjuryLog log) {
        Integer layoff = null;
        LocalDate end = log.getActualReturnedAt() != null ? log.getActualReturnedAt() : LocalDate.now();
        if (log.getStartedAt() != null) {
            layoff = (int) ChronoUnit.DAYS.between(log.getStartedAt(), end);
        }

        return AthleteInjuryDto.builder()
                .id(log.getId())
                .playerId(log.getPlayer().getId())
                .injuryType(log.getInjuryType())
                .description(log.getDescription())
                .severity(log.getSeverity())
                .startedAt(log.getStartedAt())
                .expectedReturnAt(log.getExpectedReturnAt())
                .actualReturnedAt(log.getActualReturnedAt())
                .isRecurring(log.getIsRecurring())
                .layoffDays(layoff)
                .build();
    }

    private static String toFlagEmoji(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) return null;
        String upper = countryCode.toUpperCase();
        int firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6;
        int secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstChar)) + new String(Character.toChars(secondChar));
    }
}
