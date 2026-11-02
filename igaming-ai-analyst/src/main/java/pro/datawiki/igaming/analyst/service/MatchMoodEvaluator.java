package pro.datawiki.igaming.analyst.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.MatchBroadcastDto;
import pro.datawiki.igaming.dto.mood.MatchMood;
import pro.datawiki.igaming.dto.mood.MatchMoodCardDto;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Core domain evaluator for the Match Mood Discovery engine.
 * Computes deterministic mood matches, normalized excitement scores (1.0 to 10.0),
 * and human-friendly preview hooks without full-text queries.
 */
@Slf4j
@Component
public class MatchMoodEvaluator {

    @Data
    @Builder
    public static class MatchContext {
        private Long matchId;
        private String matchSlug;
        private String sportName;
        private String sportSlug;
        private String leagueName;
        private String countryName;
        private String team1Name;
        private String team2Name;
        private LocalDateTime matchStartTime;
        private String matchStatus; // SCHEDULED, LIVE, FINISHED
        private String currentScore;

        // Tactical & statistical parameters
        private Double expectedTotalXg;
        private Double oddsP1;
        private Double oddsPx;
        private Double oddsP2;
        private Double oddsOver25;
        private Double winLeverageHome;
        private Double winLeverageAway;
        private Boolean isDerby;
        private Double derbyFrictionIndex;
        private Double compositeCardBias;
        private Double refereeAvgYellowCards;
        private Double highEventRate;
        private Double lateGoalsRatio;
        private Double benchAttackDepth;
        private Double comebackRate;

        // Streaming metadata
        private Boolean hasBroadcast;
        private List<MatchBroadcastDto> broadcasts;
        private String primaryStreamUrl;
        private String primaryStreamProvider;
    }

    /**
     * Evaluates whether a match matches a specific mood preset and constructs a detailed card.
     */
    public Optional<MatchMoodCardDto> evaluateForMood(MatchContext ctx, MatchMood targetMood) {
        if (ctx == null) {
            return Optional.empty();
        }

        return switch (targetMood) {
            case GOAL_FIESTA -> evaluateGoalFiesta(ctx);
            case GRUDGE_MATCH -> evaluateGrudgeMatch(ctx);
            case UPSET_WATCH -> evaluateUpsetWatch(ctx);
            case LATE_CLUTCH -> evaluateLateClutch(ctx);
        };
    }

    /**
     * Finds the primary matching mood (if any) with the highest excitement score.
     */
    public Optional<MatchMoodCardDto> evaluateBestMood(MatchContext ctx) {
        if (ctx == null) {
            return Optional.empty();
        }

        List<MatchMoodCardDto> candidates = new ArrayList<>();
        evaluateGoalFiesta(ctx).ifPresent(candidates::add);
        evaluateGrudgeMatch(ctx).ifPresent(candidates::add);
        evaluateUpsetWatch(ctx).ifPresent(candidates::add);
        evaluateLateClutch(ctx).ifPresent(candidates::add);

        return candidates.stream()
                .max(Comparator.comparingDouble(MatchMoodCardDto::getExcitementScore));
    }

    // ── Mood Evaluators ──────────────────────────────────────────────────────────

    private Optional<MatchMoodCardDto> evaluateGoalFiesta(MatchContext ctx) {
        double xg = ctx.getExpectedTotalXg() != null ? ctx.getExpectedTotalXg() : 2.50;
        double highEv = ctx.getHighEventRate() != null ? ctx.getHighEventRate() : 0.25;
        double oddsO25 = ctx.getOddsOver25() != null ? ctx.getOddsOver25() : 1.95;

        // Match criteria: expected xG >= 2.85 OR high event frequency >= 0.40 OR sharp bookmaker Total Over 2.5 <= 1.70
        boolean isGoalFiesta = xg >= 2.85 || highEv >= 0.38 || (oddsO25 > 1.0 && oddsO25 <= 1.72);
        if (!isGoalFiesta) {
            return Optional.empty();
        }

        double baseScore = 6.8 + Math.min(2.4, (xg - 2.5) * 1.6) + (highEv * 0.8);
        if (Boolean.TRUE.equals(ctx.getHasBroadcast())) {
            baseScore += 0.5;
        }
        double excitement = clampScore(baseScore);

        String hook = String.format(
                Locale.US,
                "Идеальный матч под пиво: команды забили на оборону, суммарный xG %.2f гарантирует шквал моментов и голы в обе стороны!",
                xg
        );

        return Optional.of(buildCard(
                ctx,
                MatchMood.GOAL_FIESTA,
                excitement,
                hook,
                "🍿 Град голов",
                "Тотал больше 2.5 / Обе забьют",
                Math.max(1.65, oddsO25),
                "Высокая вероятность результативного матча"
        ));
    }

    private Optional<MatchMoodCardDto> evaluateGrudgeMatch(MatchContext ctx) {
        boolean isDerby = Boolean.TRUE.equals(ctx.getIsDerby());
        boolean isLatin = isLatinContext(ctx);
        double derbyFriction = ctx.getDerbyFrictionIndex() != null ? ctx.getDerbyFrictionIndex() : 0.25;
        double cardBias = ctx.getCompositeCardBias() != null ? ctx.getCompositeCardBias() : 0.50;
        double refYc = ctx.getRefereeAvgYellowCards() != null ? ctx.getRefereeAvgYellowCards() : 4.0;

        // Match criteria: derby rivalry OR Latin heated match OR high friction OR high card bias with strict ref
        boolean isGrudge = isDerby || isLatin || derbyFriction >= 0.70 || cardBias >= 0.68 || refYc >= 5.0;
        if (!isGrudge) {
            return Optional.empty();
        }

        double baseScore = 7.0 + (isDerby || isLatin ? 1.2 : 0.5) + (cardBias * 1.2) + Math.min(0.8, (refYc - 4.0) * 0.4);
        if (Boolean.TRUE.equals(ctx.getHasBroadcast())) {
            baseScore += 0.5;
        }
        double excitement = clampScore(baseScore);

        String hook;
        if (isLatin) {
            hook = "🔥 Жара на поле! Латиноамериканская рубка с файерами и жесткими стыками, а комментатор на чистом испанском срывает голос — идеальный матч под пиво, чтобы орать у экрана!";
        } else if (isDerby) {
            hook = "Принципиальное дерби с запредельным нервом: строгий арбитр и ожесточенная борьба за территорию гарантируют россыпь карточек и стычек.";
        } else {
            hook = "Высокий градус дисциплинарного напряжения: жесткий стиль отбора обещает частые остановки, стычки и карточки.";
        }

        return Optional.of(buildCard(
                ctx,
                MatchMood.GRUDGE_MATCH,
                excitement,
                hook,
                "🥊 Жара и стычки",
                "Тотал желтых карточек больше 4.5",
                1.85,
                "Высокая плотность фолов и строгий рефери"
        ));
    }

    private Optional<MatchMoodCardDto> evaluateUpsetWatch(MatchContext ctx) {
        double p1 = ctx.getOddsP1() != null ? ctx.getOddsP1() : 2.50;
        double p2 = ctx.getOddsP2() != null ? ctx.getOddsP2() : 2.50;
        double levH = ctx.getWinLeverageHome() != null ? ctx.getWinLeverageHome() : 0.50;
        double levA = ctx.getWinLeverageAway() != null ? ctx.getWinLeverageAway() : 0.50;

        boolean homeFav = p1 < p2;
        double favOdd = homeFav ? p1 : p2;
        double dogOdd = homeFav ? p2 : p1;
        double favLev = homeFav ? levH : levA;
        double dogLev = homeFav ? levA : levH;

        // Disparity check: clear favorite (odd <= 1.65) and hungry underdog (dogLev > favLev + 0.35)
        boolean hasOddsDisparity = favOdd <= 1.65 && dogOdd >= 4.50;
        boolean hasMotivationDisparity = dogLev >= 0.70 && (favLev <= 0.45 || (dogLev - favLev) >= 0.35);

        if (!hasOddsDisparity && !hasMotivationDisparity) {
            return Optional.empty();
        }

        double deltaLev = Math.max(0.0, dogLev - favLev);
        double baseScore = 7.2 + (deltaLev * 1.8) + (dogOdd >= 5.0 ? 0.6 : 0.2);
        if (Boolean.TRUE.equals(ctx.getHasBroadcast())) {
            baseScore += 0.5;
        }
        double excitement = clampScore(baseScore);

        String underdogName = homeFav ? ctx.getTeam2Name() : ctx.getTeam1Name();
        String favoriteName = homeFav ? ctx.getTeam1Name() : ctx.getTeam2Name();

        String hook = String.format(
                "Классическая ловушка для фаворита: «%s» решает второстепенные задачи или бережет лидеров, а «%s» отчаянно бьется за жизненно важные очки.",
                favoriteName != null ? favoriteName : "Фаворит",
                underdogName != null ? underdogName : "Андердог"
        );

        return Optional.of(buildCard(
                ctx,
                MatchMood.UPSET_WATCH,
                excitement,
                hook,
                "💣 Охота на фаворита",
                String.format("Фора (+1.5) на %s", underdogName != null ? underdogName : "андердога"),
                1.90,
                "Критический перекос турнирной мотивации в пользу аутсайдера"
        ));
    }

    private Optional<MatchMoodCardDto> evaluateLateClutch(MatchContext ctx) {
        double lateRatio = ctx.getLateGoalsRatio() != null ? ctx.getLateGoalsRatio() : 0.21;
        double benchDepth = ctx.getBenchAttackDepth() != null ? ctx.getBenchAttackDepth() : 0.50;
        double comeback = ctx.getComebackRate() != null ? ctx.getComebackRate() : 0.20;

        // Criteria: frequent late goals (>= 25% post-75') OR deep attacking bench (>= 0.68)
        boolean isClutch = lateRatio >= 0.26 || benchDepth >= 0.68 || comeback >= 0.32;
        if (!isClutch) {
            return Optional.empty();
        }

        double baseScore = 6.6 + (lateRatio * 3.5) + (benchDepth * 1.2) + (comeback * 0.8);
        if (Boolean.TRUE.equals(ctx.getHasBroadcast())) {
            baseScore += 0.5;
        }
        double excitement = clampScore(baseScore);

        String hook = "Матч поздней драмы: команды регулярно переворачивают ход встреч после 75-й минуты благодаря взрывным джокерам в линии атаки.";

        return Optional.of(buildCard(
                ctx,
                MatchMood.LATE_CLUTCH,
                excitement,
                hook,
                "⏱️ Драма в концовке",
                "2-й тайм результативнее 1-го / Гол после 75'",
                2.05,
                "Высокая активность свежих форвардов со скамейки против уставшей обороны"
        ));
    }

    private double clampScore(double score) {
        double rounded = Math.round(score * 10.0) / 10.0;
        return Math.max(1.0, Math.min(10.0, rounded));
    }

    private MatchMoodCardDto buildCard(
            MatchContext ctx,
            MatchMood mood,
            double excitementScore,
            String viewingHook,
            String moodBadgeLabel,
            String casualBetMarket,
            double casualBetOdd,
            String casualBetRationale
    ) {
        return MatchMoodCardDto.builder()
                .matchId(ctx.getMatchId())
                .matchSlug(ctx.getMatchSlug())
                .sportName(ctx.getSportName())
                .sportSlug(ctx.getSportSlug())
                .leagueName(ctx.getLeagueName())
                .countryName(ctx.getCountryName())
                .team1Name(ctx.getTeam1Name())
                .team2Name(ctx.getTeam2Name())
                .matchStartTime(ctx.getMatchStartTime())
                .matchStatus(ctx.getMatchStatus())
                .currentScore(ctx.getCurrentScore())
                .matchedMood(mood)
                .excitementScore(excitementScore)
                .viewingHook(viewingHook)
                .moodBadgeLabel(moodBadgeLabel)
                .expectedTotalXg(ctx.getExpectedTotalXg())
                .winLeverageHome(ctx.getWinLeverageHome())
                .winLeverageAway(ctx.getWinLeverageAway())
                .isDerby(ctx.getIsDerby())
                .cardsExpectancy(ctx.getRefereeAvgYellowCards())
                .lateGoalProbability(ctx.getLateGoalsRatio())
                .hasBroadcast(ctx.getHasBroadcast())
                .broadcasts(ctx.getBroadcasts())
                .primaryStreamUrl(ctx.getPrimaryStreamUrl())
                .primaryStreamProvider(ctx.getPrimaryStreamProvider())
                .casualBetMarket(casualBetMarket)
                .casualBetOdd(casualBetOdd)
                .casualBetRationale(casualBetRationale)
                .build();
    }

    private boolean isLatinContext(MatchContext ctx) {
        String all = ((ctx.getTeam1Name() != null ? ctx.getTeam1Name() : "") + " " +
                      (ctx.getTeam2Name() != null ? ctx.getTeam2Name() : "") + " " +
                      (ctx.getLeagueName() != null ? ctx.getLeagueName() : "")).toLowerCase(java.util.Locale.ROOT);
        return all.contains("santiago") || all.contains("buenos") || all.contains("aires") ||
               all.contains("boca") || all.contains("river") || all.contains("argentin") ||
               all.contains("chile") || all.contains("brazil") || all.contains("brasil") ||
               all.contains("flamengo") || all.contains("palmeiras") || all.contains("libertadores") ||
               all.contains("racing") || all.contains("colo");
    }
}
