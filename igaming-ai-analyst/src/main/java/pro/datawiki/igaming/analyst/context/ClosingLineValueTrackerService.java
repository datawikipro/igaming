package pro.datawiki.igaming.analyst.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.entity.MatchExternalContextEntity;
import pro.datawiki.igaming.analyst.repository.MatchExternalContextRepository;
import pro.datawiki.igaming.analyst.service.odds.MarketOddsResolver;
import pro.datawiki.igaming.analyst.service.odds.MarketQuoteDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Institutional Closing Line Value (CLV) Tracker.
 * <p>
 * Every 2 minutes scans upcoming matches starting within [-2 min .. +15 min] that already have
 * a persisted {@code match_external_context} snapshot without a closing anchor.
 * Captures the live Pinnacle/Sharp consensus odd right before kickoff and calculates:
 * {@code clv_pct = ((executable_odd / closing_sharp_odd) - 1.0) * 100.0}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClosingLineValueTrackerService {

    private final JdbcTemplate jdbcTemplate;
    private final MatchExternalContextRepository contextRepository;
    private final MarketOddsResolver marketOddsResolver;

    @Scheduled(fixedDelay = 120_000, initialDelay = 45_000)
    public void captureClosingLinesNearKickoff() {
        try {
            List<Map<String, Object>> candidates = jdbcTemplate.queryForList("""
                    SELECT DISTINCT c.match_id, c.target_market
                    FROM match_external_context c
                    JOIN match_ai_recommendation r ON r.match_id = c.match_id
                    WHERE c.closing_sharp_odd IS NULL
                      AND c.target_market IS NOT NULL
                      AND c.executable_odd IS NOT NULL
                      AND c.executable_odd > 1.01
                      AND r.match_start_time IS NOT NULL
                      AND r.match_start_time BETWEEN NOW() - INTERVAL '2 minutes' AND NOW() + INTERVAL '15 minutes'
                    LIMIT 60
                    """);

            if (candidates.isEmpty()) return;

            int captured = 0;
            OffsetDateTime now = OffsetDateTime.now();

            for (Map<String, Object> row : candidates) {
                Long matchId = row.get("match_id") instanceof Number n ? n.longValue() : null;
                String targetMarket = row.get("target_market") != null ? String.valueOf(row.get("target_market")) : null;
                if (matchId == null || targetMarket == null || targetMarket.isBlank()) continue;

                Optional<MarketQuoteDto> quoteOpt = marketOddsResolver.resolveQuote(matchId, targetMarket);
                if (quoteOpt.isEmpty()) continue;

                MarketQuoteDto q = quoteOpt.get();
                double closingOdd = q.consensusValue() > 1.01 ? q.consensusValue() : q.value();
                if (closingOdd <= 1.01) continue;

                Optional<MatchExternalContextEntity> ctxOpt = contextRepository.findById(matchId);
                if (ctxOpt.isEmpty()) continue;

                MatchExternalContextEntity ctx = ctxOpt.get();
                if (ctx.getClosingSharpOdd() != null) continue;

                double execOdd = ctx.getExecutableOdd() != null ? ctx.getExecutableOdd().doubleValue() : 0.0;
                ctx.setClosingSharpOdd(BigDecimal.valueOf(closingOdd).setScale(3, RoundingMode.HALF_UP));
                ctx.setClosingCapturedAt(now);
                if (execOdd > 1.01) {
                    double clvPct = ((execOdd / closingOdd) - 1.0) * 100.0;
                    ctx.setClvPct(BigDecimal.valueOf(clvPct).setScale(2, RoundingMode.HALF_UP));
                }
                ctx.setUpdatedAt(now);
                contextRepository.save(ctx);
                captured++;
            }

            if (captured > 0) {
                log.info("🎯 [CLV-TRACKER] Captured closing Pinnacle/Sharp anchor for {} matches near kickoff.", captured);
            }
        } catch (Exception e) {
            log.debug("[CLV-TRACKER] Scheduled sweep skipped: {}", e.getMessage());
        }
    }
}
