package pro.datawiki.igaming.analyst.rating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal Dynamic Sports MMR Service.
 * Manages active player ratings across overall and surface-specific dimensions.
 * Provides high-speed in-memory resolution for ML vector extraction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SportMmrService {

    private final SportMmrRatingRepository ratingRepository;
    private final SportMmrHistoryRepository historyRepository;
    private final TournamentTierClassifier tierClassifier;
    private final MultiTierGlickoEngine glickoEngine;
    private final pro.datawiki.igaming.analyst.ml.vector.ClickHouseDdsClient clickHouse;

    // High-speed In-Memory Cache: "sport:surface:playerId" -> GlickoPlayer
    private final Map<String, GlickoPlayer> cache = new ConcurrentHashMap<>();

    private String buildCacheKey(String sportSlug, String surface, long playerId) {
        if (sportSlug == null || sportSlug.isBlank()) {
            throw new IllegalArgumentException("sportSlug is required and cannot be null or blank");
        }
        return sportSlug + ":" + (surface != null && !surface.isBlank() ? surface : "ALL") + ":" + playerId;
    }

    /**
     * Resolves player rating with fallback to default prior (1500 MMR, 350 RD).
     */
    public GlickoPlayer getOrLoadPlayer(long playerId, String sportSlug, String surface) {
        if (sportSlug == null || sportSlug.isBlank()) {
            throw new IllegalArgumentException("sportSlug is required and cannot be null or blank");
        }
        if (surface == null || surface.isBlank()) {
            throw new IllegalArgumentException("surface is required and cannot be null or blank");
        }
        String cleanSurface = surface.toUpperCase(Locale.ROOT);
        String key = buildCacheKey(sportSlug, cleanSurface, playerId);
        return cache.computeIfAbsent(key, k -> {
            Optional<SportMmrRatingEntity> entityOpt = ratingRepository.findByPlayerIdAndSportSlugAndSurface(
                    playerId, sportSlug, cleanSurface
            );
            if (entityOpt.isPresent()) {
                return entityOpt.get().toGlickoPlayer();
            }
            return GlickoPlayer.ofDefault(playerId, sportSlug, cleanSurface);
        });
    }

    /**
     * Processes a single match outcome and updates ratings across overall and surface categories.
     * Guaranteed strictly idempotent: skips execution if matchId was already recorded in history.
     */
    @Transactional
    public void processMatchOutcome(
            Long matchId,
            long player1Id,
            long player2Id,
            int score1,
            int score2,
            String sportSlug,
            String surface,
            Long tournamentId,
            String tournamentName,
            Instant matchTime,
            double movMultiplier,
            String p1Name,
            String p2Name,
            String p1Country,
            String p2Country
    ) {
        if (matchId != null && matchId > 0 && historyRepository.existsByMatchId(matchId)) {
            log.debug("Skipping MMR update for match #{}: already recorded in sport_mmr_history", matchId);
            return;
        }

        if (sportSlug == null || sportSlug.isBlank()) {
            throw new IllegalArgumentException("sportSlug is required and cannot be null or blank");
        }
        if (surface == null || surface.isBlank()) {
            throw new IllegalArgumentException("surface is required and cannot be null or blank");
        }
        String surf = surface.toUpperCase(Locale.ROOT);
        TournamentTierClassifier.TierInfo tier = tierClassifier.classify(tournamentId, tournamentName, sportSlug);

        // 1. Update Surface-Specific Rating if applicable
        if (!"ALL".equals(surf)) {
            updatePair(matchId, player1Id, player2Id, score1, score2, sportSlug, surf, tier, movMultiplier, matchTime, p1Name, p2Name, p1Country, p2Country);
        }

        // 2. Update Overall (ALL) Rating
        updatePair(matchId, player1Id, player2Id, score1, score2, sportSlug, "ALL", tier, movMultiplier, matchTime, p1Name, p2Name, p1Country, p2Country);
    }

    @Transactional
    public void processMatchOutcome(
            Long matchId,
            long player1Id,
            long player2Id,
            int score1,
            int score2,
            String sportSlug,
            String surface,
            String tournamentName,
            Instant matchTime,
            double movMultiplier,
            String p1Name,
            String p2Name,
            String p1Country,
            String p2Country
    ) {
        processMatchOutcome(matchId, player1Id, player2Id, score1, score2, sportSlug, surface, null, tournamentName, matchTime, movMultiplier, p1Name, p2Name, p1Country, p2Country);
    }

    /**
     * High-level entrypoint for completed canonical matches (emitted from Kafka or settlement).
     */
    @Transactional
    public void processFinishedMdmMatch(
            Long matchId,
            String sportSlug,
            Long tournamentId,
            String tournamentName,
            long p1Id,
            long p2Id,
            int score1,
            int score2,
            Instant matchTime,
            String p1Name,
            String p2Name,
            String p1Country,
            String p2Country,
            List<pro.datawiki.igaming.dto.live.PeriodScoreDto> periods
    ) {
        if (p1Id <= 0 || p2Id <= 0 || (score1 == 0 && score2 == 0)) {
            return;
        }
        if (matchId != null && matchId > 0 && historyRepository.existsByMatchId(matchId)) {
            return;
        }

        double mov = 1.0;
        if (periods != null && !periods.isEmpty()) {
            int pointDiff = 0;
            for (pro.datawiki.igaming.dto.live.PeriodScoreDto p : periods) {
                pointDiff += (p.getScore1() - p.getScore2());
            }
            if ("tennis".equalsIgnoreCase(sportSlug)) {
                mov = 1.0 + Math.min(0.30, Math.abs(pointDiff) * 0.02);
            } else if ("table-tennis".equalsIgnoreCase(sportSlug)) {
                mov = 1.0 + Math.min(0.25, Math.abs(pointDiff) * 0.015);
            }
        } else {
            int setDiff = Math.abs(score1 - score2);
            mov = 1.0 + Math.min(0.20, setDiff * 0.08);
        }

        processMatchOutcome(
                matchId, p1Id, p2Id, score1, score2,
                sportSlug, "ALL", tournamentId, tournamentName,
                matchTime != null ? matchTime : Instant.now(),
                mov, p1Name, p2Name, p1Country, p2Country
        );
    }

    @Transactional
    public void processFinishedMdmMatch(
            Long matchId,
            String sportSlug,
            String tournamentName,
            long p1Id,
            long p2Id,
            int score1,
            int score2,
            Instant matchTime,
            String p1Name,
            String p2Name,
            String p1Country,
            String p2Country,
            List<pro.datawiki.igaming.dto.live.PeriodScoreDto> periods
    ) {
        processFinishedMdmMatch(matchId, sportSlug, null, tournamentName, p1Id, p2Id, score1, score2, matchTime, p1Name, p2Name, p1Country, p2Country, periods);
    }

    private void updatePair(
            Long matchId,
            long p1Id,
            long p2Id,
            int score1,
            int score2,
            String sportSlug,
            String surface,
            TournamentTierClassifier.TierInfo tier,
            double movMultiplier,
            Instant matchTime,
            String p1Name,
            String p2Name,
            String p1Country,
            String p2Country
    ) {
        GlickoPlayer p1 = getOrLoadPlayer(p1Id, sportSlug, surface);
        GlickoPlayer p2 = getOrLoadPlayer(p2Id, sportSlug, surface);

        double mmr1Before = p1.getMmr();
        double rd1Before = p1.getRd();
        double mmr2Before = p2.getMmr();
        double rd2Before = p2.getRd();

        MultiTierGlickoEngine.MatchUpdateResult res = glickoEngine.updateMatch(
                p1, p2, score1, score2, tier, movMultiplier
        );

        p1.setLastMatchTime(matchTime);
        p2.setLastMatchTime(matchTime);

        // Save entities in PostgreSQL
        SportMmrRatingEntity ent1 = SportMmrRatingEntity.fromGlickoPlayer(p1, p1Name, p1Country);
        SportMmrRatingEntity ent2 = SportMmrRatingEntity.fromGlickoPlayer(p2, p2Name, p2Country);
        ratingRepository.save(ent1);
        ratingRepository.save(ent2);

        // Async sync to ClickHouse DDS ReplacingMergeTree
        if (clickHouse != null) {
            clickHouse.upsertSportMmrRating(ent1);
            clickHouse.upsertSportMmrRating(ent2);
        }

        // Log history audit
        if (matchId != null && matchId > 0) {
            historyRepository.save(SportMmrHistoryEntity.builder()
                    .matchId(matchId)
                    .playerId(p1Id)
                    .sportSlug(sportSlug)
                    .surface(surface)
                    .matchTime(matchTime != null ? matchTime : Instant.now())
                    .mmrBefore(mmr1Before)
                    .mmrAfter(p1.getMmr())
                    .rdBefore(rd1Before)
                    .rdAfter(p1.getRd())
                    .deltaMmr(res.deltaP1())
                    .opponentId(p2Id)
                    .opponentMmr(mmr2Before)
                    .tierLevel(tier.tierLevel())
                    .build());

            historyRepository.save(SportMmrHistoryEntity.builder()
                    .matchId(matchId)
                    .playerId(p2Id)
                    .sportSlug(sportSlug)
                    .surface(surface)
                    .matchTime(matchTime != null ? matchTime : Instant.now())
                    .mmrBefore(mmr2Before)
                    .mmrAfter(p2.getMmr())
                    .rdBefore(rd2Before)
                    .rdAfter(p2.getRd())
                    .deltaMmr(res.deltaP2())
                    .opponentId(p1Id)
                    .opponentMmr(mmr1Before)
                    .tierLevel(tier.tierLevel())
                    .build());
        }
    }
}
