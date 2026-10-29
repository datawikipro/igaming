package pro.datawiki.igaming.analyst.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
        return filter(status, tier, sport, stage, timeRange, null, null, query);
    }

    public static Specification<MatchAiRecommendation> filter(
            String status,
            String tier,
            String sport,
            String stage,
            String timeRange,
            String oddRange,
            String query) {
        return filter(status, tier, sport, stage, timeRange, oddRange, null, query);
    }

    public static Specification<MatchAiRecommendation> filter(
            String status,
            String tier,
            String sport,
            String stage,
            String timeRange,
            String oddRange,
            String bookmaker,
            String query) {

        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Status Filter
            if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                String s = status.toUpperCase().trim();
                if ("SETTLED".equalsIgnoreCase(s) || "PAST".equalsIgnoreCase(s) || "HISTORY".equalsIgnoreCase(s)) {
                    predicates.add(root.get("resultStatus").in("WON", "LOST", "VOID"));
                } else if ("ACTIVE".equalsIgnoreCase(s) || "OPPORTUNITIES".equalsIgnoreCase(s) || "OPPORTUNITY".equalsIgnoreCase(s)) {
                    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                    predicates.add(cb.equal(root.get("resultStatus"), "PENDING"));
                    predicates.add(cb.notEqual(root.get("convictionTier"), "PASS"));
                    predicates.add(cb.notEqual(root.get("recommendedMarket"), "PASS"));
                    predicates.add(cb.not(root.get("evaluationStage").in(
                            "SETTLED", "AWAITING_SETTLEMENT", "CANCELLED_SCHEDULE_CONFLICT",
                            "CANCELLED_SYNTHETIC_COLLISION", "SCHEDULE_CONFLICT_PENDING_RESOLUTION"
                    )));
                    predicates.add(cb.or(
                            root.get("finalScore").isNull(),
                            cb.equal(cb.trim(root.get("finalScore")), "")
                    ));
                    predicates.add(cb.or(
                            cb.greaterThan(root.get("matchStartTime"), now),
                            buildActiveInPlayPredicate(root, cb, now)
                    ));
                } else if ("PASS".equalsIgnoreCase(s) || "SKIPPED".equalsIgnoreCase(s)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("resultStatus"), "SKIPPED"),
                            cb.equal(root.get("convictionTier"), "PASS"),
                            cb.equal(root.get("recommendedMarket"), "PASS")
                    ));
                } else if ("NOT_PASS".equalsIgnoreCase(s) || "NO_PASS".equalsIgnoreCase(s) || "NON_PASS".equalsIgnoreCase(s)) {
                    predicates.add(cb.and(
                            cb.notEqual(root.get("resultStatus"), "SKIPPED"),
                            cb.notEqual(root.get("convictionTier"), "PASS"),
                            cb.notEqual(root.get("recommendedMarket"), "PASS")
                    ));
                } else {
                    predicates.add(cb.equal(cb.upper(root.get("resultStatus")), s));
                }
            }

            // 2. ML Tier Filter
            if (tier != null && !tier.isBlank() && !"ALL".equalsIgnoreCase(tier)) {
                String t = tier.toUpperCase().trim();
                if ("ML_90".equals(t)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("convictionTier"), "ML_90"),
                            cb.greaterThanOrEqualTo(root.get("confidenceScore"), 90)
                    ));
                    predicates.add(cb.notEqual(root.get("resultStatus"), "SKIPPED"));
                    predicates.add(cb.notEqual(root.get("convictionTier"), "PASS"));
                    predicates.add(cb.notEqual(root.get("recommendedMarket"), "PASS"));
                } else if ("ML_80".equals(t)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("convictionTier"), "ML_80"),
                            cb.between(root.get("confidenceScore"), 80, 89)
                    ));
                    predicates.add(cb.notEqual(root.get("resultStatus"), "SKIPPED"));
                    predicates.add(cb.notEqual(root.get("convictionTier"), "PASS"));
                    predicates.add(cb.notEqual(root.get("recommendedMarket"), "PASS"));
                } else if ("ML_70".equals(t)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("convictionTier"), "ML_70"),
                            cb.between(root.get("confidenceScore"), 70, 79)
                    ));
                    predicates.add(cb.notEqual(root.get("resultStatus"), "SKIPPED"));
                    predicates.add(cb.notEqual(root.get("convictionTier"), "PASS"));
                    predicates.add(cb.notEqual(root.get("recommendedMarket"), "PASS"));
                } else if ("ML_60".equals(t)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("convictionTier"), "ML_60"),
                            cb.between(root.get("confidenceScore"), 60, 69)
                    ));
                    predicates.add(cb.notEqual(root.get("resultStatus"), "SKIPPED"));
                    predicates.add(cb.notEqual(root.get("convictionTier"), "PASS"));
                    predicates.add(cb.notEqual(root.get("recommendedMarket"), "PASS"));
                } else if ("PASS".equals(t)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("resultStatus"), "SKIPPED"),
                            cb.equal(root.get("convictionTier"), "PASS"),
                            cb.equal(root.get("recommendedMarket"), "PASS")
                    ));
                } else if ("NOT_PASS".equals(t) || "NO_PASS".equals(t) || "ACTIVE".equals(t) || "ACTIVE_COHORTS".equals(t) || "NON_PASS".equals(t)) {
                    predicates.add(cb.and(
                            cb.notEqual(root.get("resultStatus"), "SKIPPED"),
                            cb.notEqual(root.get("convictionTier"), "PASS"),
                            cb.notEqual(root.get("recommendedMarket"), "PASS")
                    ));
                }
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
                    predicates.add(cb.notEqual(cb.coalesce(root.get("evaluationStage"), "PRE_LINEUP"), "IN_PLAY"));
                    predicates.add(cb.greaterThan(root.get("matchStartTime"), now));
                } else if ("IN_PLAY".equals(st)) {
                    predicates.add(buildActiveInPlayPredicate(root, cb, now));
                } else if ("CONFLICT".equals(st) || "SCHEDULE_CONFLICT".equals(st)) {
                    predicates.add(cb.or(
                            cb.equal(root.get("evaluationStage"), "SCHEDULE_CONFLICT_PENDING_RESOLUTION"),
                            cb.equal(root.get("resultStatus"), "CONFLICT")
                    ));
                } else if ("LINEUP_CONFIRMED".equals(st)) {
                    predicates.add(cb.and(
                            cb.notEqual(cb.coalesce(root.get("evaluationStage"), "PRE_LINEUP"), "IN_PLAY"),
                            cb.notEqual(cb.coalesce(root.get("lineupStatus"), "ESTIMATED"), "PROJECTED"),
                            cb.or(
                                    cb.equal(root.get("evaluationStage"), "LINEUP_CONFIRMED"),
                                    cb.equal(root.get("evaluationStage"), "STAGE_2_PREVIEW"),
                                    cb.equal(root.get("lineupStatus"), "CONFIRMED")
                            )
                    ));
                } else if ("KICKOFF_VERIFIED".equals(st)) {
                    predicates.add(cb.equal(root.get("evaluationStage"), "KICKOFF_VERIFIED"));
                    predicates.add(cb.greaterThan(root.get("matchStartTime"), now.minusMinutes(5)));
                } else if ("WAIT_KICKOFF".equals(st)) {
                    predicates.add(cb.equal(root.get("evaluationStage"), "WAIT_KICKOFF"));
                    predicates.add(cb.greaterThan(root.get("matchStartTime"), now.minusMinutes(2)));
                } else if ("PRE_LINEUP".equals(st)) {
                    predicates.add(cb.and(
                            cb.greaterThan(root.get("matchStartTime"), now),
                            cb.notEqual(cb.coalesce(root.get("lineupStatus"), "ESTIMATED"), "PROJECTED"),
                            cb.notEqual(cb.coalesce(root.get("lineupStatus"), "ESTIMATED"), "CONFIRMED"),
                            cb.notEqual(cb.coalesce(root.get("evaluationStage"), "PRE_LINEUP"), "LINEUP_CONFIRMED"),
                            cb.notEqual(cb.coalesce(root.get("evaluationStage"), "PRE_LINEUP"), "KICKOFF_VERIFIED"),
                            cb.notEqual(cb.coalesce(root.get("evaluationStage"), "PRE_LINEUP"), "STAGE_2_PREVIEW"),
                            cb.notEqual(cb.coalesce(root.get("evaluationStage"), "PRE_LINEUP"), "IN_PLAY")
                    ));
                }
            }

            // 5. Time Range Filter
            if (timeRange != null && !timeRange.isBlank() && !"ALL".equalsIgnoreCase(timeRange)) {
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                String tr = timeRange.toUpperCase().trim();
                boolean isTerminalStatus = status != null && (
                        "SETTLED".equalsIgnoreCase(status) ||
                        "WON".equalsIgnoreCase(status) ||
                        "LOST".equalsIgnoreCase(status) ||
                        "VOID".equalsIgnoreCase(status)
                );

                if ("LIVE".equals(tr)) {
                    predicates.add(buildActiveInPlayPredicate(root, cb, now));
                } else if ("T_1H".equals(tr) || "1H".equals(tr)) {
                    if (isTerminalStatus) {
                        predicates.add(cb.between(root.get("matchStartTime"), now.minusHours(1), now));
                    } else {
                        predicates.add(cb.between(root.get("matchStartTime"), now, now.plusHours(1)));
                    }
                } else if ("T_4H".equals(tr) || "4H".equals(tr)) {
                    if (isTerminalStatus) {
                        predicates.add(cb.between(root.get("matchStartTime"), now.minusHours(4), now));
                    } else {
                        predicates.add(cb.between(root.get("matchStartTime"), now, now.plusHours(4)));
                    }
                } else if ("T_24H".equals(tr) || "24H".equals(tr) || "TODAY".equals(tr)) {
                    // "Сегодня (< 24ч)": All matches within 24 hours (both past 24h and next 24h)
                    predicates.add(cb.between(root.get("matchStartTime"), now.minusHours(24), now.plusHours(24)));
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

            // 7. Bookmaker Filter (OOP Domain Resolution)
            if (bookmaker != null && !bookmaker.isBlank() && !"ALL".equalsIgnoreCase(bookmaker)) {
                String bm = bookmaker.trim();
                if ("HAS_BOOKMAKER".equalsIgnoreCase(bm) || "WITH_ODDS".equalsIgnoreCase(bm) || "BOOKMAKERS_ONLY".equalsIgnoreCase(bm)) {
                    predicates.add(cb.or(
                            cb.and(root.get("availableBookmakers").isNotNull(), cb.notEqual(root.get("availableBookmakers"), "")),
                            cb.and(
                                    root.get("bestBookmaker").isNotNull(),
                                    cb.notEqual(root.get("bestBookmaker"), "NONE"),
                                    cb.notEqual(root.get("bestBookmaker"), "Market Consensus"),
                                    cb.notEqual(root.get("bestBookmaker"), "Market Consensus (Fair)"),
                                    cb.notEqual(root.get("bestBookmaker"), "")
                            )
                    ));
                } else {
                    List<String> searchTerms = BookmakerQueryFilter.resolveSearchTerms(bm);
                    List<Predicate> bmDisjunction = new ArrayList<>();
                    for (String term : searchTerms) {
                        bmDisjunction.add(cb.like(cb.lower(root.get("availableBookmakers")), "%" + term + "%"));
                        bmDisjunction.add(cb.like(cb.lower(root.get("bestBookmaker")), "%" + term + "%"));
                    }
                    predicates.add(cb.or(bmDisjunction.toArray(new Predicate[0])));
                }
            }

            // 8. Text Search Query
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

    /**
     * Builds a JPA Predicate ensuring that the match is currently in-play and has NOT yet finished.
     * Respects sport-specific physical duration guards (Domain Rule #9):
     * - Table Tennis: <= 30 mins
     * - Short Football: <= 40 mins
     * - Combat / MMA / Boxing: <= 50 mins
     * - Football: <= 120 mins
     * - Basketball: <= 130 mins
     * - Hockey: <= 160 mins
     * - Tennis: <= 180 mins
     * - Default/Other: <= 120 mins
     * Excludes settled outcomes, cancelled matches, matches with recorded final scores,
     * and matches marked as awaiting settlement.
     */
    private static Predicate buildActiveInPlayPredicate(
            Root<MatchAiRecommendation> root,
            CriteriaBuilder cb,
            LocalDateTime now) {

        Predicate started = cb.lessThanOrEqualTo(root.get("matchStartTime"), now);
        Predicate notSettled = cb.and(
                cb.not(root.get("resultStatus").in("WON", "LOST", "VOID")),
                cb.notEqual(cb.coalesce(root.get("resultStatus"), ""), "CANCELLED"),
                cb.notEqual(cb.coalesce(root.get("resultStatus"), ""), "CONFLICT"),
                cb.notEqual(cb.coalesce(root.get("resultStatus"), ""), "SKIPPED"),
                cb.notEqual(cb.coalesce(root.get("evaluationStage"), ""), "SETTLED"),
                cb.notEqual(cb.coalesce(root.get("evaluationStage"), ""), "AWAITING_SETTLEMENT"),
                cb.notEqual(cb.coalesce(root.get("evaluationStage"), ""), "CANCELLED_SCHEDULE_CONFLICT"),
                cb.notEqual(cb.coalesce(root.get("evaluationStage"), ""), "CANCELLED_SYNTHETIC_COLLISION"),
                cb.notEqual(cb.coalesce(root.get("evaluationStage"), ""), "SCHEDULE_CONFLICT_PENDING_RESOLUTION")
        );
        Predicate scoreEmpty = cb.or(
                cb.isNull(root.get("finalScore")),
                cb.equal(cb.trim(root.get("finalScore")), "")
        );

        Predicate isTableTennis = cb.and(
                cb.equal(cb.upper(root.get("sportName")), "TABLE_TENNIS"),
                cb.greaterThanOrEqualTo(root.get("matchStartTime"), now.minusMinutes(30))
        );
        Predicate isShortFootball = cb.and(
                cb.equal(cb.upper(root.get("sportName")), "SHORT_FOOTBALL"),
                cb.greaterThanOrEqualTo(root.get("matchStartTime"), now.minusMinutes(40))
        );
        Predicate isCombat = cb.and(
                cb.or(
                        cb.equal(cb.upper(root.get("sportName")), "COMBAT"),
                        cb.equal(cb.upper(root.get("sportName")), "MMA"),
                        cb.equal(cb.upper(root.get("sportName")), "BOXING")
                ),
                cb.greaterThanOrEqualTo(root.get("matchStartTime"), now.minusMinutes(50))
        );
        Predicate isFootball = cb.and(
                cb.equal(cb.upper(root.get("sportName")), "FOOTBALL"),
                cb.greaterThanOrEqualTo(root.get("matchStartTime"), now.minusMinutes(120))
        );
        Predicate isBasketball = cb.and(
                cb.equal(cb.upper(root.get("sportName")), "BASKETBALL"),
                cb.greaterThanOrEqualTo(root.get("matchStartTime"), now.minusMinutes(130))
        );
        Predicate isHockey = cb.and(
                cb.or(
                        cb.equal(cb.upper(root.get("sportName")), "HOCKEY"),
                        cb.equal(cb.upper(root.get("sportName")), "ICE_HOCKEY")
                ),
                cb.greaterThanOrEqualTo(root.get("matchStartTime"), now.minusMinutes(160))
        );
        Predicate isTennis = cb.and(
                cb.equal(cb.upper(root.get("sportName")), "TENNIS"),
                cb.greaterThanOrEqualTo(root.get("matchStartTime"), now.minusMinutes(180))
        );
        Predicate isOtherSport = cb.and(
                cb.not(cb.upper(root.get("sportName")).in(
                        "TABLE_TENNIS", "SHORT_FOOTBALL", "COMBAT", "MMA", "BOXING",
                        "FOOTBALL", "BASKETBALL", "HOCKEY", "ICE_HOCKEY", "TENNIS"
                )),
                cb.greaterThanOrEqualTo(root.get("matchStartTime"), now.minusMinutes(120))
        );

        Predicate withinSportDuration = cb.or(
                isTableTennis, isShortFootball, isCombat, isFootball,
                isBasketball, isHockey, isTennis, isOtherSport
        );

        return cb.and(started, notSettled, scoreEmpty, withinSportDuration);
    }
}
