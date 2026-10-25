package pro.datawiki.igaming.mdm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.mdm.domain.*;
import pro.datawiki.igaming.mdm.repository.*;

import java.util.Optional;

/**
 * Seeds and maintains the SofaScore Golden Record catalog into the MDM database.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SofaScoreSeederService {

    private final MdmSportRepository sportRepository;
    private final MdmCategoryRepository categoryRepository;
    private final MdmTournamentRepository tournamentRepository;
    private final MdmTeamRepository teamRepository;
    private final MdmPlayerRepository playerRepository;
    private final MdmTeamMembershipRepository teamMembershipRepository;
    private final MdmTeamTranslationRepository teamTranslationRepository;
    private final MdmTournamentTranslationRepository tournamentTranslationRepository;

    @Transactional
    public MdmSport seedSport(String slug, String domainKey, boolean isEsports, boolean isTeamSport, int sortOrder) {
        return sportRepository.findBySlug(slug).orElseGet(() -> {
            log.info("Seeding MDM Sport: slug={}, domainKey={}", slug, domainKey);
            return sportRepository.save(MdmSport.builder()
                    .slug(slug)
                    .domainKey(domainKey)
                    .isEsports(isEsports)
                    .isTeamSport(isTeamSport)
                    .sortOrder(sortOrder)
                    .build());
        });
    }

    @Transactional
    public MdmCategory seedCategory(MdmSport sport, String slug, String code, Long sofascoreId, String flagCode) {
        return categoryRepository.findBySportIdAndSlug(sport.getId(), slug).orElseGet(() -> {
            log.info("Seeding MDM Category: slug={}, code={}, sofascoreId={}", slug, code, sofascoreId);
            return categoryRepository.save(MdmCategory.builder()
                    .sport(sport)
                    .slug(slug)
                    .code(code)
                    .sofascoreId(sofascoreId)
                    .flagCode(flagCode)
                    .sourceOrigin("SOFASCORE")
                    .build());
        });
    }

    @Transactional
    public MdmTournament seedTournament(
            MdmSport sport,
            MdmCategory category,
            String slug,
            Long sofascoreId,
            String canonicalName,
            int tier,
            String gender,
            String ageCategory,
            boolean isCup,
            int popularityRank,
            boolean isFeatured,
            String logoUrl
    ) {
        Optional<MdmTournament> existing = tournamentRepository.findBySofascoreId(sofascoreId);
        if (existing.isPresent()) {
            return existing.get();
        }

        log.info("Seeding MDM Tournament: name='{}', sofascoreId={}, slug={}", canonicalName, sofascoreId, slug);
        MdmTournament tournament = tournamentRepository.save(MdmTournament.builder()
                .sport(sport)
                .category(category)
                .slug(slug)
                .sofascoreId(sofascoreId)
                .canonicalName(canonicalName)
                .tier(tier)
                .gender(gender)
                .ageCategory(ageCategory)
                .isCup(isCup)
                .popularityRank(popularityRank)
                .isFeatured(isFeatured)
                .logoUrl(logoUrl)
                .sourceOrigin("SOFASCORE")
                .build());

        // Default English translation
        tournamentTranslationRepository.save(MdmTournamentTranslation.builder()
                .tournament(tournament)
                .lang("en")
                .name(canonicalName)
                .build());

        return tournament;
    }

    @Transactional
    public MdmTeam seedTeam(
            MdmSport sport,
            MdmCategory category,
            String slug,
            Long sofascoreId,
            String canonicalName,
            boolean isNational,
            boolean isIndividual,
            String gender,
            String ageCategory,
            String normalizedSignature,
            String logoUrl,
            int popularityRank
    ) {
        Optional<MdmTeam> existing = teamRepository.findBySofascoreId(sofascoreId);
        if (existing.isPresent()) {
            return existing.get();
        }

        log.info("Seeding MDM Team: name='{}', sofascoreId={}, national={}", canonicalName, sofascoreId, isNational);
        MdmTeam team = teamRepository.save(MdmTeam.builder()
                .sport(sport)
                .category(category)
                .slug(slug)
                .sofascoreId(sofascoreId)
                .canonicalName(canonicalName)
                .isNational(isNational)
                .isIndividual(isIndividual)
                .gender(gender)
                .ageCategory(ageCategory)
                .normalizedSignature(normalizedSignature)
                .logoUrl(logoUrl)
                .popularityRank(popularityRank)
                .sourceOrigin("SOFASCORE")
                .build());

        // Default English translation
        teamTranslationRepository.save(MdmTeamTranslation.builder()
                .team(team)
                .lang("en")
                .name(canonicalName)
                .build());

        return team;
    }

    @Transactional
    public void addTranslation(Long teamId, String lang, String name, String shortName) {
        teamRepository.findById(teamId).ifPresent(team -> {
            if (teamTranslationRepository.findByTeamIdAndLang(teamId, lang).isEmpty()) {
                teamTranslationRepository.save(MdmTeamTranslation.builder()
                        .team(team)
                        .lang(lang)
                        .name(name)
                        .shortName(shortName)
                        .build());
            }
        });
    }

    @Transactional
    public void registerMembership(Long playerId, Long teamId, String membershipType, Integer jerseyNumber, String role) {
        if (teamMembershipRepository.findByPlayerIdAndMembershipTypeAndIsActiveTrue(playerId, membershipType).isEmpty()) {
            playerRepository.findById(playerId).ifPresent(player ->
                    teamRepository.findById(teamId).ifPresent(team ->
                            teamMembershipRepository.save(MdmTeamMembership.builder()
                                    .player(player)
                                    .team(team)
                                    .membershipType(membershipType)
                                    .jerseyNumber(jerseyNumber)
                                    .squadRole(role)
                                    .isActive(true)
                                    .build())));
        }
    }
}
