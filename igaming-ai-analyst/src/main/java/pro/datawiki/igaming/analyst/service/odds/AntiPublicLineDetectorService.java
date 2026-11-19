package pro.datawiki.igaming.analyst.service.odds;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * Real-Time Anti-Public & Sharp-vs-Soft Line Movement Detector.
 * <p>
 * Tracks opening consensus odds per match/market in Redis ({@code odds:open:<matchId>}, TTL 72h)
 * and compares live bookmaker quotes against:
 * 1. Opening Line Drift: Detects when recreational public money hammers the favorite,
 *    pushing the opposite side up into the anti-public sweet spot (2.00 - 2.80) while
 *    real-time MMR / ML probability confirms objective value ({@code ANTI_PUBLIC_FADE}).
 * 2. Sharp-vs-Soft Discrepancy: Detects when soft bookmakers price a market significantly
 *    above the Pinnacle / Bet365 / Betfair no-vig anchor ({@code SHARP_VS_SOFT_VALUE}).
 * 3. Sharp Steam Confirmation: Detects when consensus drops sharply in the direction of
 *    our signal ({@code SHARP_STEAM_CONFIRM}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntiPublicLineDetectorService {

    private static final String OPENING_ODDS_PREFIX = "odds:open:";
    private static final Duration OPENING_TTL = Duration.ofHours(72);

    /** Minimum upward drift (%) on our leg indicating public steam on the opposite side. */
    public static final double ANTI_PUBLIC_DRIFT_MIN_PCT = 4.0;
    /** Minimum downward drop (%) on our leg indicating sharp steam confirmation. */
    public static final double SHARP_STEAM_DROP_PCT = -4.0;
    /** Minimum executable-vs-sharp divergence (%) for sharp-anchored value signal. */
    public static final double SHARP_VS_SOFT_MIN_DIV_PCT = 2.5;
    /** Empirical anti-public sweet-spot odd range where bookmaker public shading creates structural +EV. */
    public static final double SWEET_SPOT_MIN_ODD = 1.90;
    public static final double SWEET_SPOT_MAX_ODD = 3.00;
    /** Approximate sharp-bookmaker margin removal factor (~2.5% vig). */
    private static final double SHARP_NO_VIG_FACTOR = 0.975;

    private final StringRedisTemplate redisTemplate;
    private final MarketOddsResolver marketOddsResolver;

    public record AntiPublicSignal(
            String steamDirection,
            double openingOdd,
            double currentConsensusOdd,
            double executableOdd,
            double driftPct,
            double sharpVsSoftDivergencePct,
            boolean sharpAnchored,
            boolean antiPublicSweetSpot,
            String rationaleNote
    ) {
        public static AntiPublicSignal neutral() {
            return new AntiPublicSignal("NEUTRAL", 0.0, 0.0, 0.0, 0.0, 0.0, false, false, null);
        }
    }

    /**
     * Evaluates line movement, sharp-vs-soft divergence, and anti-public sweet spot alignment.
     */
    public AntiPublicSignal analyzeMarketMovement(Long matchId, String targetMarket, double mlFairProbPct) {
        if (matchId == null || targetMarket == null || targetMarket.isBlank()) {
            return AntiPublicSignal.neutral();
        }

        Optional<MarketQuoteDto> quoteOpt;
        try {
            quoteOpt = marketOddsResolver.resolveQuote(matchId, targetMarket);
        } catch (Exception e) {
            log.debug("[ANTI-PUBLIC] Could not resolve quote for match #{}, market={}: {}", matchId, targetMarket, e.getMessage());
            return AntiPublicSignal.neutral();
        }

        if (quoteOpt.isEmpty()) {
            return AntiPublicSignal.neutral();
        }

        MarketQuoteDto quote = quoteOpt.get();
        double execOdd = quote.value();
        double consensusOdd = quote.consensusValue() > 1.01 ? quote.consensusValue() : execOdd;
        double divergencePct = quote.divergencePct();
        boolean isSharpAnchored = quote.isSharpAnchored();

        double openingOdd = recordOrLoadOpeningOdd(matchId, targetMarket, consensusOdd);
        double driftPct = (openingOdd > 1.01)
                ? round2(((consensusOdd - openingOdd) / openingOdd) * 100.0)
                : 0.0;

        double impliedProbPct = 100.0 / Math.max(1.01, execOdd);
        boolean mmrConfirmsEdge = mlFairProbPct > (impliedProbPct + 3.0);
        boolean inSweetSpot = execOdd >= SWEET_SPOT_MIN_ODD && execOdd <= SWEET_SPOT_MAX_ODD;

        // Sharp No-Vig EV: if Pinnacle/Bet365/Betfair sets consensus, check if soft book beats sharp no-vig price
        double sharpNoVigProb = (1.0 / Math.max(1.01, consensusOdd)) * SHARP_NO_VIG_FACTOR;
        double sharpVsSoftEvPct = round2((sharpNoVigProb * execOdd - 1.0) * 100.0);

        // Priority 1: ANTI_PUBLIC_FADE (public money hammered opposite side, pushing our leg up while MMR confirms value)
        if (driftPct >= ANTI_PUBLIC_DRIFT_MIN_PCT && mmrConfirmsEdge) {
            String note = String.format(Locale.US,
                    "⚡ ANTI-PUBLIC FADE: кэф вырос от открытия %.2f → %.2f (%+.1f%% на прогрузе публики в обратное плечо), real-time MMR/ML подтверждает перевес (P=%.1f%% vs рынок %.1f%%).",
                    openingOdd, consensusOdd, driftPct, mlFairProbPct, impliedProbPct);
            return new AntiPublicSignal(
                    "ANTI_PUBLIC_FADE", openingOdd, consensusOdd, execOdd,
                    driftPct, divergencePct, isSharpAnchored, inSweetSpot, note
            );
        }

        // Priority 2: SHARP_VS_SOFT_VALUE (soft bookmaker is lagging/shaded above Sharp Pinnacle/Bet365 anchor)
        if (isSharpAnchored && (divergencePct >= SHARP_VS_SOFT_MIN_DIV_PCT || sharpVsSoftEvPct >= 1.0)) {
            String note = String.format(Locale.US,
                    "🎯 SHARP-VS-SOFT VALUE: %s даёт %.2f против Sharp-якоря (Pinnacle/Bet365) %.2f (%+.1f%% перекос, No-Vig EV %+.1f%%).",
                    quote.bookmaker(), execOdd, consensusOdd, divergencePct, sharpVsSoftEvPct);
            return new AntiPublicSignal(
                    "SHARP_VS_SOFT_VALUE", openingOdd, consensusOdd, execOdd,
                    driftPct, divergencePct, true, inSweetSpot, note
            );
        }

        // Priority 3: SHARP_STEAM_CONFIRM (line moved sharply toward our signal)
        if (driftPct <= SHARP_STEAM_DROP_PCT) {
            String note = String.format(Locale.US,
                    "📉 SHARP STEAM CONFIRM: рынок прогружается в сторону прогноза (%.2f → %.2f, %.1f%%).",
                    openingOdd, consensusOdd, driftPct);
            return new AntiPublicSignal(
                    "SHARP_STEAM_CONFIRM", openingOdd, consensusOdd, execOdd,
                    driftPct, divergencePct, isSharpAnchored, inSweetSpot, note
            );
        }

        // Priority 4: PUBLIC_DRIFT_UP (line drifted up without strong MMR confirmation — caution)
        if (driftPct >= ANTI_PUBLIC_DRIFT_MIN_PCT) {
            String note = String.format(Locale.US,
                    "⚠️ DRIFT UP: кэф вырос %.2f → %.2f (%+.1f%%) без выраженного перевеса MMR.",
                    openingOdd, consensusOdd, driftPct);
            return new AntiPublicSignal(
                    "DRIFT_UP", openingOdd, consensusOdd, execOdd,
                    driftPct, divergencePct, isSharpAnchored, inSweetSpot, note
            );
        }

        return new AntiPublicSignal(
                "NEUTRAL", openingOdd, consensusOdd, execOdd,
                driftPct, divergencePct, isSharpAnchored, inSweetSpot, null
        );
    }

    /**
     * Atomically stores the first observed consensus odd via HSETNX so the opening price
     * is never overwritten during the match lifecycle.
     */
    double recordOrLoadOpeningOdd(Long matchId, String market, double currentConsensus) {
        if (redisTemplate == null || currentConsensus <= 1.01) {
            return currentConsensus;
        }
        String key = OPENING_ODDS_PREFIX + matchId;
        String field = market.trim().toUpperCase(Locale.ROOT);
        try {
            Boolean inserted = redisTemplate.opsForHash().putIfAbsent(key, field, String.format(Locale.US, "%.4f", currentConsensus));
            if (Boolean.TRUE.equals(inserted)) {
                redisTemplate.expire(key, OPENING_TTL);
                return currentConsensus;
            }
            Object existing = redisTemplate.opsForHash().get(key, field);
            if (existing != null) {
                double parsed = Double.parseDouble(existing.toString());
                if (parsed > 1.01) {
                    return parsed;
                }
            }
        } catch (Exception e) {
            log.debug("[ANTI-PUBLIC] Redis opening odd lookup failed for match #{}, market={}: {}", matchId, market, e.getMessage());
        }
        return currentConsensus;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
