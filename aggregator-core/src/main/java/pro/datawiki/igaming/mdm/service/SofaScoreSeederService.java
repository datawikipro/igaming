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

import java.util.Optional;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.mdm.domain.MdmCategory;
import pro.datawiki.igaming.mdm.domain.MdmPlayer;
import pro.datawiki.igaming.mdm.domain.MdmSport;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTeamMembership;
import pro.datawiki.igaming.mdm.domain.MdmTeamTranslation;
import pro.datawiki.igaming.mdm.domain.MdmTournament;
import pro.datawiki.igaming.mdm.domain.MdmTournamentTranslation;
import pro.datawiki.igaming.mdm.repository.MdmCategoryRepository;
import pro.datawiki.igaming.mdm.repository.MdmPlayerRepository;
import pro.datawiki.igaming.mdm.repository.MdmSportRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamMembershipRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamTranslationRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentTranslationRepository;

@Service
public class SofaScoreSeederService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(SofaScoreSeederService.class);
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
        return this.sportRepository.findBySlug(slug).orElseGet(() -> {
            log.info("Seeding MDM Sport: slug={}, domainKey={}", (Object)slug, (Object)domainKey);
            return (MdmSport)this.sportRepository.save(MdmSport.builder().slug(slug).domainKey(domainKey).isEsports(isEsports).isTeamSport(isTeamSport).sortOrder(sortOrder).build());
        });
    }

    @Transactional
    public MdmCategory seedCategory(MdmSport sport, String slug, String code, Long sofascoreId, String flagCode) {
        return this.categoryRepository.findBySportIdAndSlug(sport.getId(), slug).orElseGet(() -> {
            log.info("Seeding MDM Category: slug={}, code={}, sofascoreId={}", new Object[]{slug, code, sofascoreId});
            return (MdmCategory)this.categoryRepository.save(MdmCategory.builder().sport(sport).slug(slug).code(code).sofascoreId(sofascoreId).flagCode(flagCode).sourceOrigin("SOFASCORE").build());
        });
    }

    @Transactional
    public MdmTournament seedTournament(MdmSport sport, MdmCategory category, String slug, Long sofascoreId, String canonicalName, int tier, String gender, String ageCategory, boolean isCup, int popularityRank, boolean isFeatured, String logoUrl) {
        Optional<MdmTournament> existing = this.tournamentRepository.findBySofascoreId(sofascoreId);
        if (existing.isPresent()) {
            return existing.get();
        }
        log.info("Seeding MDM Tournament: name='{}', sofascoreId={}, slug={}", new Object[]{canonicalName, sofascoreId, slug});
        MdmTournament tournament = (MdmTournament)this.tournamentRepository.save(MdmTournament.builder().sport(sport).category(category).slug(slug).sofascoreId(sofascoreId).canonicalName(canonicalName).tier(tier).gender(gender).ageCategory(ageCategory).isCup(isCup).popularityRank(popularityRank).isFeatured(isFeatured).logoUrl(logoUrl).sourceOrigin("SOFASCORE").build());
        this.tournamentTranslationRepository.save(MdmTournamentTranslation.builder().tournament(tournament).lang("en").name(canonicalName).build());
        return tournament;
    }

    @Transactional
    public MdmTeam seedTeam(MdmSport sport, MdmCategory category, String slug, Long sofascoreId, String canonicalName, boolean isNational, boolean isIndividual, String gender, String ageCategory, String normalizedSignature, String logoUrl, int popularityRank) {
        Optional<MdmTeam> existing = this.teamRepository.findBySofascoreId(sofascoreId);
        if (existing.isPresent()) {
            return existing.get();
        }
        log.info("Seeding MDM Team: name='{}', sofascoreId={}, national={}", new Object[]{canonicalName, sofascoreId, isNational});
        MdmTeam team = (MdmTeam)this.teamRepository.save(MdmTeam.builder().sport(sport).category(category).slug(slug).sofascoreId(sofascoreId).canonicalName(canonicalName).isNational(isNational).isIndividual(isIndividual).gender(gender).ageCategory(ageCategory).normalizedSignature(normalizedSignature).logoUrl(logoUrl).popularityRank(popularityRank).sourceOrigin("SOFASCORE").build());
        this.teamTranslationRepository.save(MdmTeamTranslation.builder().team(team).lang("en").name(canonicalName).build());
        return team;
    }

    @Transactional
    public void addTranslation(Long teamId, String lang, String name, String shortName) {
        this.teamRepository.findById(teamId).ifPresent(team -> {
            if (this.teamTranslationRepository.findByTeamIdAndLang(teamId, lang).isEmpty()) {
                this.teamTranslationRepository.save(MdmTeamTranslation.builder().team((MdmTeam)team).lang(lang).name(name).shortName(shortName).build());
            }
        });
    }

    @Transactional
    public void registerMembership(Long playerId, Long teamId, String membershipType, Integer jerseyNumber, String role) {
        if (this.teamMembershipRepository.findByPlayerIdAndMembershipTypeAndIsActiveTrue(playerId, membershipType).isEmpty()) {
            this.playerRepository.findById(playerId).ifPresent(player -> this.teamRepository.findById(teamId).ifPresent(team -> this.teamMembershipRepository.save(MdmTeamMembership.builder().player((MdmPlayer)player).team((MdmTeam)team).membershipType(membershipType).jerseyNumber(jerseyNumber).squadRole(role).isActive(true).build())));
        }
    }

    @Generated
    public SofaScoreSeederService(MdmSportRepository sportRepository, MdmCategoryRepository categoryRepository, MdmTournamentRepository tournamentRepository, MdmTeamRepository teamRepository, MdmPlayerRepository playerRepository, MdmTeamMembershipRepository teamMembershipRepository, MdmTeamTranslationRepository teamTranslationRepository, MdmTournamentTranslationRepository tournamentTranslationRepository) {
        this.sportRepository = sportRepository;
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.teamTranslationRepository = teamTranslationRepository;
        this.tournamentTranslationRepository = tournamentTranslationRepository;
    }
}

