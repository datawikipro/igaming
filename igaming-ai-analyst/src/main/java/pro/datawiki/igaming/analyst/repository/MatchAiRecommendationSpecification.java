package pro.datawiki.igaming.analyst.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MatchAiRecommendationSpecification {

    private static final Set<String> MAIN_SPORTS = Set.of(
            "FOOTBALL", "HOCKEY", "BASKETBALL", "TENNIS", "TABLE_TENNIS", "MMA", "COMBAT", "ESPORTS", "SHORT_FOOTBALL"
    );

    public static Specification<MatchAiRecommendation> filter(
            String status,
            String tier,
            String sport,
            String stage,
            String timeRange,
            String query) {
        return filter(status, tier, sport, stage, timeRange, null, query);
    }

    public static Specification<MatchAiRecommendation> filter(
            String status,
            String tier,
            String sport,
            String stage,
            String timeRange,
            String oddRange,
            String query) {

        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Status Filter
            if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                if ("SETTLED".equalsIgnoreCase(status)) {
                    predicates.add(root.get("resultStatus").in("WON", "LOST", "VOID"));
                } else {
                    predicates.add(cb.equal(cb.upper(root.get("resultStatus")), status.toUpperCase().trim()));
                }
            }

            // 2. ML Tier Filter
            if (tier != null && !tier.isBlank() && !"ALL".equalsIgnoreCase(tier)) {
                String t = tier.toUpperCase().trim();
                if ("ML_90".equals(t)) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("confidenceScore"), 90));
                    predicates.add(cb.notEqual(root.get("resultStatus"), "SKIPPED"));
                } else if ("ML_80".equals(t)) {
                    predicates.add(cb.between(root.get("confidenceScore"), 80, 89));
                    predicates.add(cb.notEqual(root.get("resultStatus"), "SKIPPED"));
                } else if ("ML_70".equals(t) || "CONFIDENT".equals(t)) {
                    predicates.add(cb.between(root.get("confidenceScore"), 70, 79));
                    predicates.add(cb.notEqual(root.get("resultStatus"), "SKIPPED"));
                } else if ("ML_60".equals(t) || "GOOD_PICK".equals(t)) {
                    predicates.add(cb.between(root.get("confidenceScore"), 60, 69));
                    predicates.add(cb.notEqual(root.get("resultStatus"), "SKIPPED"));
                } else if ("PASS".equals(t)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("resultStatus"), "SKIPPED"),
                            cb.equal(root.get("convictionTier"), "PASS")
                    ));
                }
            } else if ((status == null || "ALL".equalsIgnoreCase(status)) && (tier == null || "ALL".equalsIgnoreCase(tier))) {
                // If neither status nor tier specified, and default view: exclude SKIPPED from main live view unless explicitly filtered
                // (or keep top active & settled)
            }

            // 3. Sport Filter
            if (sport != null && !sport.isBlank() && !"ALL".equalsIgnoreCase(sport)) {
                String sp = sport.toUpperCase().trim();
                if ("OTHER".equals(sp)) {
                    predicates.add(cb.not(cb.upper(root.get("sportName")).in(MAIN_SPORTS)));
                } else {
                    predicates.add(cb.equal(cb.upper(root.get("sportName")), sp));
                }
            }

            // 4. Stage Filter
            if (stage != null && !stage.isBlank() && !"ALL".equalsIgnoreCase(stage)) {
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                String st = stage.toUpperCase().trim();
                if ("PROJECTED".equals(st)) {
                    predicates.add(cb.equal(root.get("lineupStatus"), "PROJECTED"));
                } else if ("LINEUP_CONFIRMED".equals(st)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("evaluationStage"), "LINEUP_CONFIRMED"),
                            cb.equal(root.get("evaluationStage"), "KICKOFF_VERIFIED"),
                            cb.equal(root.get("evaluationStage"), "STAGE_2_PREVIEW"),
                            cb.equal(root.get("lineupStatus"), "CONFIRMED")
                    ));
                } else if ("KICKOFF_VERIFIED".equals(st)) {
                    predicates.add(cb.equal(root.get("evaluationStage"), "KICKOFF_VERIFIED"));
                } else if ("WAIT_KICKOFF".equals(st)) {
                    predicates.add(cb.equal(root.get("evaluationStage"), "WAIT_KICKOFF"));
                } else if ("PRE_LINEUP".equals(st)) {
                    predicates.add(cb.and(
                            cb.greaterThan(root.get("matchStartTime"), now),
                            cb.notEqual(cb.coalesce(root.get("lineupStatus"), "ESTIMATED"), "PROJECTED"),
                            cb.notEqual(cb.coalesce(root.get("lineupStatus"), "ESTIMATED"), "CONFIRMED"),
                            cb.notEqual(cb.coalesce(root.get("evaluationStage"), "PRE_LINEUP"), "LINEUP_CONFIRMED"),
                            cb.notEqual(cb.coalesce(root.get("evaluationStage"), "PRE_LINEUP"), "KICKOFF_VERIFIED"),
                            cb.notEqual(cb.coalesce(root.get("evaluationStage"), "PRE_LINEUP"), "STAGE_2_PREVIEW")
                    ));
                }
            }

            // 5. Time Range Filter
            if (timeRange != null && !timeRange.isBlank() && !"ALL".equalsIgnoreCase(timeRange)) {
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                String tr = timeRange.toUpperCase().trim();
                if ("LIVE".equals(tr)) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("matchStartTime"), now));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("matchStartTime"), now.minusMinutes(150)));
                    predicates.add(cb.equal(root.get("resultStatus"), "PENDING"));
                } else if ("T_1H".equals(tr) || "1H".equals(tr)) {
                    predicates.add(cb.between(root.get("matchStartTime"), now, now.plusHours(1)));
                } else if ("T_4H".equals(tr) || "4H".equals(tr)) {
                    predicates.add(cb.between(root.get("matchStartTime"), now, now.plusHours(4)));
                } else if ("T_24H".equals(tr) || "24H".equals(tr) || "TODAY".equals(tr)) {
                    predicates.add(cb.between(root.get("matchStartTime"), now, now.plusHours(24)));
                } else if ("T_MORE".equals(tr) || "MORE".equals(tr)) {
                    predicates.add(cb.greaterThan(root.get("matchStartTime"), now.plusHours(24)));
                }
            }

            // 6. Odds Filter (1.3-1.8, 1.8-2.2, 2.2-3.0, 3.0+)
            if (oddRange != null && !oddRange.isBlank() && !"ALL".equalsIgnoreCase(oddRange)) {
                String or = oddRange.trim();
                if ("1.3-1.8".equals(or) || "ODD_1_3_1_8".equalsIgnoreCase(or)) {
                    predicates.add(cb.between(root.get("recommendedOdd"), new BigDecimal("1.30"), new BigDecimal("1.80")));
                } else if ("1.8-2.2".equals(or) || "ODD_1_8_2_2".equalsIgnoreCase(or)) {
                    predicates.add(cb.between(root.get("recommendedOdd"), new BigDecimal("1.80"), new BigDecimal("2.20")));
                } else if ("2.2-3.0".equals(or) || "ODD_2_2_3_0".equalsIgnoreCase(or)) {
                    predicates.add(cb.between(root.get("recommendedOdd"), new BigDecimal("2.20"), new BigDecimal("3.00")));
                } else if ("3.0+".equals(or) || "3.0_PLUS".equalsIgnoreCase(or) || "3+".equals(or)) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("recommendedOdd"), new BigDecimal("3.00")));
                }
            }

            // 7. Text Search Query
            if (query != null && !query.isBlank()) {
                String qPattern = "%" + query.toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("team1Name")), qPattern),
                        cb.like(cb.lower(root.get("team2Name")), qPattern),
                        cb.like(cb.lower(root.get("leagueName")), qPattern),
                        cb.like(cb.lower(root.get("recommendedMarket")), qPattern),
                        cb.like(cb.lower(root.get("coreHypothesis")), qPattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
