package pro.datawiki.igaming.analyst.ml.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bottom-Up Squad Chemistry & International Fatigue ("Вирус сборных") Evaluator.
 * Computes empirical partnership synergy, tenure in club, debutant blunder risk,
 * and travel fatigue decay from recent national team fixtures.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SquadChemistryEvaluator {

    private final ClickHouseDdsClient clickHouse;

    // Fast in-memory cache for player memberships: id -> facts
    private final Map<Long, List<ClickHouseDdsClient.PlayerMembershipFact>> membershipCache = new ConcurrentHashMap<>();

    public record SquadChemistryAndFatigue(
            float squadTenureScore,          // 0.0 .. 1.0 (average starter matches in club / 25.0)
            float cbPairingSolidity,         // 0.45 .. 0.92 (tenure + synergy of center-backs)
            float midfieldScreenSolidity,    // 0.48 .. 0.90 (tenure + synergy of midfielders)
            float blunderRiskMultiplier,     // 0.80 .. 1.35 (elevated if new transfers / inexperience)
            int nationalFatiguedCount,       // count of starters with recent national team match
            float staminaDiscount,           // 0.88 .. 1.00 (discount on starter ratings due to FIFA virus)
            float lateDefenseLeakBonus       // 0.00 .. +0.15 (extra defense fatigue leak in 75-90 min)
    ) {
        public static SquadChemistryAndFatigue neutral() {
            return new SquadChemistryAndFatigue(0.60f, 0.68f, 0.68f, 1.0f, 0, 1.0f, 0.0f);
        }
    }

    /**
     * Evaluates squad chemistry, partnership synergy, and national team travel fatigue for starting players.
     */
    public SquadChemistryAndFatigue evaluateSquadChemistry(
            Long teamId,
            List<Map<String, Object>> starters,
            long matchTs
    ) {
        if (starters == null || starters.isEmpty()) {
            return SquadChemistryAndFatigue.neutral();
        }

        // Collect player IDs to prefetch
        Set<Long> idsToFetch = new HashSet<>();
        for (Map<String, Object> p : starters) {
            Long sofaId = extractId(p.get("sofascore_id"), p.get("sofa_id"));
            Long mdmId = extractId(p.get("id"), p.get("player_id"));
            if (sofaId != null && sofaId > 0 && !membershipCache.containsKey(sofaId)) idsToFetch.add(sofaId);
            if (mdmId != null && mdmId > 0 && !membershipCache.containsKey(mdmId)) idsToFetch.add(mdmId);
        }

        if (!idsToFetch.isEmpty()) {
            prefetchMemberships(idsToFetch);
        }

        List<Integer> clubMatches = new ArrayList<>();
        List<Integer> cbMatches = new ArrayList<>();
        List<Integer> midMatches = new ArrayList<>();
        int newTransfersCount = 0;
        int nationalFatiguedCount = 0;

        for (Map<String, Object> p : starters) {
            Long sofaId = extractId(p.get("sofascore_id"), p.get("sofa_id"));
            Long mdmId = extractId(p.get("id"), p.get("player_id"));

            List<ClickHouseDdsClient.PlayerMembershipFact> playerMemberships = findMemberships(sofaId, mdmId);

            // 1. Club tenure analysis
            ClickHouseDdsClient.PlayerMembershipFact clubFact = null;
            ClickHouseDdsClient.PlayerMembershipFact nationalFact = null;

            for (ClickHouseDdsClient.PlayerMembershipFact f : playerMemberships) {
                if ("NATIONAL_TEAM".equalsIgnoreCase(f.membershipType())) {
                    nationalFact = f;
                } else if ("CLUB_PRIMARY".equalsIgnoreCase(f.membershipType()) || f.membershipType().startsWith("CLUB")) {
                    if (teamId != null && teamId > 0 && f.mdmTeamId() == teamId) {
                        clubFact = f;
                    } else if (clubFact == null) {
                        clubFact = f;
                    }
                }
            }

            int matches = (clubFact != null && clubFact.totalMatches() > 0) ? clubFact.totalMatches() : 0;
            clubMatches.add(matches);

            if (matches < 4) {
                newTransfersCount++;
            }

            String pos = String.valueOf(p.getOrDefault("position", "")).trim().toUpperCase();
            TacticalLine line = TacticalLine.fromPosition(pos);
            if (line == TacticalLine.DEFENSE) {
                cbMatches.add(matches);
            } else if (line == TacticalLine.MIDFIELD) {
                midMatches.add(matches);
            }

            // 2. FIFA Virus / National team fatigue analysis
            if (nationalFact != null && nationalFact.lastMatchTs() > 0 && matchTs > 0) {
                long diffMs = matchTs - nationalFact.lastMatchTs();
                double diffDays = diffMs / 86_400_000.0;
                // National match occurred between 0 and 5.5 days before match kickoff
                if (diffDays >= 0.0 && diffDays <= 5.5) {
                    nationalFatiguedCount++;
                }
            }
        }

        // Squad tenure score: average starter matches in club normalized to 25 games baseline
        float avgMatches = average(clubMatches, 12.0f);
        float tenureScore = Math.min(1.0f, Math.max(0.10f, avgMatches / 25.0f));

        // Center-back pairing solidity: smooth linear tenure mapping without arbitrary thresholds
        float avgCb = average(cbMatches, avgMatches);
        float cbSolidity = Math.min(1.0f, Math.max(0.20f, avgCb / 25.0f));

        // Midfield screen solidity: smooth linear tenure mapping
        float avgMid = average(midMatches, avgMatches);
        float midSolidity = Math.min(1.0f, Math.max(0.20f, avgMid / 25.0f));

        // Blunder risk multiplier: smooth ratio of new transfers
        float blunderMult = 1.0f + (newTransfersCount / 11.0f);

        // International travel fatigue decay ("Вирус сборных"): smooth linear discount
        float staminaDiscount = Math.max(0.85f, 1.0f - (nationalFatiguedCount * 0.03f));
        float lateDefenseLeakBonus = Math.min(0.20f, nationalFatiguedCount * 0.035f);

        return new SquadChemistryAndFatigue(
                tenureScore,
                cbSolidity,
                midSolidity,
                blunderMult,
                nationalFatiguedCount,
                staminaDiscount,
                lateDefenseLeakBonus
        );
    }

    private List<ClickHouseDdsClient.PlayerMembershipFact> findMemberships(Long sofaId, Long mdmId) {
        if (sofaId != null && membershipCache.containsKey(sofaId)) {
            return membershipCache.get(sofaId);
        }
        if (mdmId != null && membershipCache.containsKey(mdmId)) {
            return membershipCache.get(mdmId);
        }
        return Collections.emptyList();
    }

    private void prefetchMemberships(Set<Long> ids) {
        try {
            List<ClickHouseDdsClient.PlayerMembershipFact> facts = clickHouse.getPlayerMemberships(ids);
            for (ClickHouseDdsClient.PlayerMembershipFact f : facts) {
                if (f.sofascoreId() > 0) {
                    membershipCache.computeIfAbsent(f.sofascoreId(), k -> new ArrayList<>()).add(f);
                }
                if (f.mdmPlayerId() > 0) {
                    membershipCache.computeIfAbsent(f.mdmPlayerId(), k -> new ArrayList<>()).add(f);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to prefetch player memberships from ClickHouse: {}", e.getMessage());
        }
    }

    private Long extractId(Object... objs) {
        for (Object o : objs) {
            if (o != null) {
                if (o instanceof Number n) return n.longValue();
                try {
                    return Long.parseLong(o.toString());
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private float average(List<Integer> list, float fallback) {
        if (list == null || list.isEmpty()) return fallback;
        float sum = 0.0f;
        for (int v : list) sum += v;
        return sum / list.size();
    }
}
