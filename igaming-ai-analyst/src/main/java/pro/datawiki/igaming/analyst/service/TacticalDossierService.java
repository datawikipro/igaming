package pro.datawiki.igaming.analyst.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.analyst.model.TacticalMatchDossier;
import pro.datawiki.igaming.analyst.model.TacticalTensor;
import pro.datawiki.igaming.dto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory reactive dossier and tactical tensor manager.
 * Consumes all canonical event streams directly from Kafka without synchronous HTTP calls or database locks.
 */
@Slf4j
@Service
public class TacticalDossierService {

    private final Map<String, TacticalMatchDossier> dossierCache = new ConcurrentHashMap<>();
    private final Map<String, TacticalTensor> tensorCache = new ConcurrentHashMap<>();

    /**
     * Reactively enriches match dossier and tactical tensor using canonical match xG statistics.
     */
    public TacticalMatchDossier enrichWithXg(CanonicalMatchXgDto dto) {
        String matchId = dto.getMatchId() != null ? dto.getMatchId() : dto.getExternalMatchId();
        log.info("Reactively enriching dossier with xG for matchId [{}]", matchId);

        TacticalMatchDossier dossier = dossierCache.computeIfAbsent(matchId, k -> TacticalMatchDossier.builder().matchId(k).build());

        dossier.setSport(dto.getSport());
        dossier.setCompetition(dto.getCompetition());
        dossier.setSeason(dto.getSeason());
        dossier.setHomeTeam(dto.getHomeTeam());
        dossier.setAwayTeam(dto.getAwayTeam());
        dossier.setHomeScore(dto.getHomeScore());
        dossier.setAwayScore(dto.getAwayScore());
        dossier.setHomeXg(dto.getHomeXg());
        dossier.setAwayXg(dto.getAwayXg());

        double hXg = dto.getHomeXg() != null ? dto.getHomeXg() : 0.0;
        double aXg = dto.getAwayXg() != null ? dto.getAwayXg() : 0.0;
        dossier.setXgDifferential(hXg - aXg);

        dossier.setHomeXag(dto.getHomeXag());
        dossier.setAwayXag(dto.getAwayXag());
        dossier.setHomeSca(dto.getHomeSca());
        dossier.setAwaySca(dto.getAwaySca());
        dossier.setHomeGca(dto.getHomeGca());
        dossier.setAwayGca(dto.getAwayGca());
        dossier.setHomeNpxg(dto.getHomeNpxg());
        dossier.setAwayNpxg(dto.getAwayNpxg());
        dossier.setHomePrgP(dto.getHomePrgP());
        dossier.setAwayPrgP(dto.getAwayPrgP());
        dossier.setHomePrgC(dto.getHomePrgC());
        dossier.setAwayPrgC(dto.getAwayPrgC());

        // Derive tactical classification and findings
        List<String> findings = ensureFindings(dossier);
        if (hXg > aXg + 0.8) {
            dossier.setTacticalClassification("HOME_ATTACK_DOMINANCE");
            findings.add(String.format("%s produced dominant offensive pressure (+%.2f xG diff)", dto.getHomeTeam(), hXg - aXg));
        } else if (aXg > hXg + 0.8) {
            dossier.setTacticalClassification("AWAY_ATTACK_DOMINANCE");
            findings.add(String.format("%s controlled dangerous goal situations (+%.2f xG diff)", dto.getAwayTeam(), aXg - hXg));
        } else {
            dossier.setTacticalClassification("CONTESTED_PARITY");
            findings.add("Evenly balanced match in terms of dangerous chance creation");
        }

        if (dto.getHomeScore() != null && dto.getHomeXg() != null) {
            if (dto.getHomeScore() > dto.getHomeXg() + 1.0) {
                findings.add(String.format("%s overperformed finishing efficiency (scored %d vs %.2f xG)",
                        dto.getHomeTeam(), dto.getHomeScore(), dto.getHomeXg()));
            } else if (dto.getHomeXg() > dto.getHomeScore() + 1.0) {
                findings.add(String.format("%s underperformed finishing efficiency (scored %d vs %.2f xG)",
                        dto.getHomeTeam(), dto.getHomeScore(), dto.getHomeXg()));
            }
        }

        dossier.setKeyFindings(findings);
        dossier.setLastEnrichedEpochMs(System.currentTimeMillis());

        // Update total accumulated xG in tensor
        TacticalTensor tensor = tensorCache.computeIfAbsent(matchId, this::initializeTensor);
        tensor.setTotalAccumulatedXg(hXg + aXg);
        tensor.setLastUpdatedEpochMs(System.currentTimeMillis());

        return dossier;
    }

    /**
     * Reactively computes spatial shot danger matrix and zone breakdowns from Understat shot map.
     */
    public TacticalTensor enrichWithShotMap(CanonicalShotMapDto dto) {
        String matchId = dto.getMatchId() != null ? dto.getMatchId() : dto.getExternalMatchId();
        log.info("Reactively computing tactical tensor from shot map for matchId [{}]", matchId);

        TacticalTensor tensor = tensorCache.computeIfAbsent(matchId, this::initializeTensor);
        double[][] homeMatrix = new double[10][10];
        double[][] awayMatrix = new double[10][10];

        Map<String, Double> homeZones = new HashMap<>();
        Map<String, Double> awayZones = new HashMap<>();
        double[] homeVector = new double[4]; // [left, central, right, depth]
        double[] awayVector = new double[4];

        int homeShots = 0;
        int awayShots = 0;

        if (dto.getShots() != null) {
            for (CanonicalShotMapDto.CanonicalShotDetailDto shot : dto.getShots()) {
                double x = shot.getX() != null ? shot.getX() : 0.5;
                double y = shot.getY() != null ? shot.getY() : 0.5;
                double xg = shot.getXg() != null ? shot.getXg() : 0.05;

                int row = Math.min(9, Math.max(0, (int) (x * 10)));
                int col = Math.min(9, Math.max(0, (int) (y * 10)));

                boolean isHome = "h".equalsIgnoreCase(shot.getTeamSide());
                if (isHome) {
                    homeShots++;
                    homeMatrix[row][col] += xg;
                    accumulateZone(homeZones, x, y, xg);
                    accumulateVector(homeVector, y, x, xg);
                } else {
                    awayShots++;
                    awayMatrix[row][col] += xg;
                    accumulateZone(awayZones, x, y, xg);
                    accumulateVector(awayVector, y, x, xg);
                }
            }
        }

        tensor.setHomeSpatialDangerMatrix(homeMatrix);
        tensor.setAwaySpatialDangerMatrix(awayMatrix);
        tensor.setHomeDangerZones(homeZones);
        tensor.setAwayDangerZones(awayZones);
        tensor.setHomePressureVector(homeVector);
        tensor.setAwayPressureVector(awayVector);
        tensor.setLastUpdatedEpochMs(System.currentTimeMillis());

        TacticalMatchDossier dossier = dossierCache.computeIfAbsent(matchId, k -> TacticalMatchDossier.builder().matchId(k).build());
        dossier.setTotalShots(homeShots + awayShots);
        dossier.setHomeShots(homeShots);
        dossier.setAwayShots(awayShots);

        if (dossier.getHomeScore() != null && homeShots > 0) {
            dossier.setHomeShotConversionRate((double) dossier.getHomeScore() / homeShots);
        }
        if (dossier.getAwayScore() != null && awayShots > 0) {
            dossier.setAwayShotConversionRate((double) dossier.getAwayScore() / awayShots);
        }
        dossier.setLastEnrichedEpochMs(System.currentTimeMillis());

        return tensor;
    }

    /**
     * Reactively enriches match dossier with absent key players and injury impact factors from sports.canonical.injuries.
     */
    public TacticalMatchDossier enrichWithInjuries(CanonicalInjuryReportDto dto) {
        String matchId = dto.getMatchId();
        log.info("Reactively enriching dossier with canonical injuries for matchId [{}]: homeImpact={}, awayImpact={}, netAdvantage={}",
                matchId, dto.getHomeAbsenceImpact(), dto.getAwayAbsenceImpact(), dto.getNetAdvantageHome());

        TacticalMatchDossier dossier = dossierCache.computeIfAbsent(matchId, k -> TacticalMatchDossier.builder().matchId(k).build());
        dossier.setHomeAbsenceImpact(dto.getHomeAbsenceImpact());
        dossier.setAwayAbsenceImpact(dto.getAwayAbsenceImpact());
        dossier.setNetInjuryAdvantageHome(dto.getNetAdvantageHome());
        dossier.setInjuryConfidenceScore(dto.getConfidenceScore());
        dossier.setHomeAbsentPlayers(dto.getHomeAbsentPlayers());
        dossier.setAwayAbsentPlayers(dto.getAwayAbsentPlayers());

        List<String> findings = ensureFindings(dossier);
        if (dto.getNetAdvantageHome() != null) {
            if (dto.getNetAdvantageHome() > 0.25) {
                findings.add(String.format("Critical roster depletion on away side: Net home advantage +%.2f (Home absent: %d, Away absent: %d)",
                        dto.getNetAdvantageHome(), dto.getHomeAbsentCount() != null ? dto.getHomeAbsentCount() : 0,
                        dto.getAwayAbsentCount() != null ? dto.getAwayAbsentCount() : 0));
            } else if (dto.getNetAdvantageHome() < -0.25) {
                findings.add(String.format("Heavy offensive casualties for home side: Net away advantage +%.2f",
                        -dto.getNetAdvantageHome()));
            }
        }
        dossier.setKeyFindings(findings);
        dossier.setLastEnrichedEpochMs(System.currentTimeMillis());
        return dossier;
    }

    /**
     * Reactively enriches match dossier with weather forecast and totals bias from sports.canonical.weather.
     */
    public TacticalMatchDossier enrichWithWeather(CanonicalWeatherDto dto) {
        String matchId = dto.getMatchId();
        log.info("Reactively enriching dossier with weather for matchId [{}]: condition={}, wind={} km/h, underBias={}",
                matchId, dto.getCondition(), dto.getWindSpeedKmh(), dto.getUnderBias());

        TacticalMatchDossier dossier = dossierCache.computeIfAbsent(matchId, k -> TacticalMatchDossier.builder().matchId(k).build());
        dossier.setWeatherCondition(dto.getCondition());
        dossier.setWeatherUnderBias(dto.getUnderBias());
        dossier.setWeatherWindSpeedKmh(dto.getWindSpeedKmh());
        dossier.setWeatherTemperatureC(dto.getTemperatureC());
        dossier.setWeatherPrecipitationProb(dto.getPrecipitationProbability());

        List<String> findings = ensureFindings(dossier);
        if (dto.getUnderBias() != null && Math.abs(dto.getUnderBias()) >= 0.15) {
            findings.add(String.format("Weather totals compression: %s (%s, wind %.1f km/h, under bias %.2f)",
                    dto.getCondition(), dto.getOutdoor() != null && dto.getOutdoor() ? "outdoor" : "indoor/roof",
                    dto.getWindSpeedKmh() != null ? dto.getWindSpeedKmh() : 0.0, dto.getUnderBias()));
        }
        dossier.setKeyFindings(findings);
        dossier.setLastEnrichedEpochMs(System.currentTimeMillis());
        return dossier;
    }

    /**
     * Reactively enriches match dossier with referee disciplinary profile from sports.canonical.referees.
     */
    public TacticalMatchDossier enrichWithReferee(CanonicalRefereeDto dto) {
        String matchId = dto.getMatchId();
        log.info("Reactively enriching dossier with referee profile for matchId [{}]: referee={}, strictnessIndex={}, tier={}",
                matchId, dto.getRefereeName(), dto.getStrictnessIndex(), dto.getStrictnessTier());

        TacticalMatchDossier dossier = dossierCache.computeIfAbsent(matchId, k -> TacticalMatchDossier.builder().matchId(k).build());
        dossier.setRefereeName(dto.getRefereeName());
        dossier.setRefereeStrictnessIndex(dto.getStrictnessIndex());
        dossier.setRefereeStrictnessTier(dto.getStrictnessTier());
        dossier.setRefereeYellowCardsPerMatch(dto.getYellowCardsPerMatch());
        dossier.setRefereeRedCardsPerMatch(dto.getRedCardsPerMatch());
        dossier.setRefereeFoulsPerMatch(dto.getFoulsPerMatch());
        dossier.setRefereePenaltiesPerMatch(dto.getPenaltiesPerMatch());

        List<String> findings = ensureFindings(dossier);
        if (dto.getStrictnessTier() != null) {
            findings.add(String.format("Official strictness factor: %s classified as %s (%.2f YC/game, strictness index %.1f/10)",
                    dto.getRefereeName(), dto.getStrictnessTier(),
                    dto.getYellowCardsPerMatch() != null ? dto.getYellowCardsPerMatch() : 0.0,
                    dto.getStrictnessIndex() != null ? dto.getStrictnessIndex() : 0.0));
        }
        dossier.setKeyFindings(findings);
        dossier.setLastEnrichedEpochMs(System.currentTimeMillis());
        return dossier;
    }

    /**
     * Reactively enriches match dossier with Betfair Smart Money signals from odds.smart-money.
     */
    public TacticalMatchDossier enrichWithSmartMoney(CanonicalSmartMoneyAlertDto dto) {
        String matchId = dto.getEventId() != null ? dto.getEventId() : dto.getMarketId();
        if (matchId == null) {
            matchId = dto.getAlertId();
        }
        log.info("Reactively enriching dossier with Betfair Smart Money for eventId [{}]: side={}, volumeEur={}, signalStrength={}",
                matchId, dto.getSharpSide(), dto.getMatchedVolumeEur(), dto.getSignalStrength());

        TacticalMatchDossier dossier = dossierCache.computeIfAbsent(matchId, k -> TacticalMatchDossier.builder().matchId(k).build());
        int currentAlerts = dossier.getSmartMoneyAlertCount() != null ? dossier.getSmartMoneyAlertCount() : 0;
        dossier.setSmartMoneyAlertCount(currentAlerts + 1);
        dossier.setSmartMoneyDominantSide(dto.getSharpSide());
        dossier.setSmartMoneyVolumeEur(dto.getMatchedVolumeEur());
        dossier.setSmartMoneySignalStrength(dto.getSignalStrength());

        List<String> findings = ensureFindings(dossier);
        findings.add(String.format("Exchange Sharp Flow: Influx of EUR %.0f on [%s] (signal strength %.2f, price disparity %.2f)",
                dto.getMatchedVolumeEur() != null ? dto.getMatchedVolumeEur() : 0.0,
                dto.getSharpSide(), dto.getSignalStrength() != null ? dto.getSignalStrength() : 0.0,
                dto.getPriceDisparity() != null ? dto.getPriceDisparity() : 0.0));

        dossier.setKeyFindings(findings);
        dossier.setLastEnrichedEpochMs(System.currentTimeMillis());
        return dossier;
    }

    /**
     * Reactively enriches match dossier with tennis point-by-point momentum from sports.tennis.point-by-point.
     */
    public TacticalMatchDossier enrichWithTennisPoints(CanonicalTennisPointDto dto) {
        String matchId = dto.getMatchId() != null ? dto.getMatchId() : dto.getPlayerName();
        log.info("Reactively enriching tennis dossier for [{}]: 1stServePct={}, 2ndServeWinPct={}, momentumScore={}",
                matchId, dto.getFirstServePct(), dto.getSecondServeWinPct(), dto.getMomentumScore());

        TacticalMatchDossier dossier = dossierCache.computeIfAbsent(matchId, k -> TacticalMatchDossier.builder().matchId(k).build());
        dossier.setTennisFirstServePct(dto.getFirstServePct());
        dossier.setTennisSecondServeWinPct(dto.getSecondServeWinPct());
        dossier.setTennisMomentumScore(dto.getMomentumScore());

        List<String> findings = ensureFindings(dossier);
        if (dto.getMomentumScore() != null) {
            findings.add(String.format("Tennis In-Play Momentum: Player %s momentum score %.2f (1st serve in: %.1f%%, 2nd serve win: %.1f%%)",
                    dto.getPlayerName(), dto.getMomentumScore(),
                    dto.getFirstServePct() != null ? dto.getFirstServePct() * 100 : 0.0,
                    dto.getSecondServeWinPct() != null ? dto.getSecondServeWinPct() * 100 : 0.0));
        }
        dossier.setKeyFindings(findings);
        dossier.setLastEnrichedEpochMs(System.currentTimeMillis());
        return dossier;
    }

    private List<String> ensureFindings(TacticalMatchDossier dossier) {
        List<String> list = dossier.getKeyFindings();
        if (list == null) {
            list = new ArrayList<>();
        } else {
            list = new ArrayList<>(list);
        }
        return list;
    }

    private void accumulateZone(Map<String, Double> zones, double x, double y, double xg) {
        String zoneKey;
        if (x >= 0.94 && y >= 0.36 && y <= 0.64) {
            zoneKey = "SIX_YARD_BOX";
        } else if (x >= 0.83 && y >= 0.21 && y <= 0.79) {
            zoneKey = "PENALTY_BOX";
        } else if (x < 0.83) {
            zoneKey = "LONG_RANGE";
        } else if (y < 0.21) {
            zoneKey = "LEFT_FLANK";
        } else if (y > 0.79) {
            zoneKey = "RIGHT_FLANK";
        } else {
            zoneKey = "CENTRAL_CHANNEL";
        }
        zones.merge(zoneKey, xg, Double::sum);
    }

    private void accumulateVector(double[] vector, double y, double x, double xg) {
        if (y < 0.33) {
            vector[0] += xg; // Left
        } else if (y <= 0.66) {
            vector[1] += xg; // Central
        } else {
            vector[2] += xg; // Right
        }
        vector[3] += x * xg; // Depth weight
    }

    private TacticalTensor initializeTensor(String matchId) {
        return TacticalTensor.builder()
                .matchId(matchId)
                .gridRows(10)
                .gridCols(10)
                .homeSpatialDangerMatrix(new double[10][10])
                .awaySpatialDangerMatrix(new double[10][10])
                .homeDangerZones(new HashMap<>())
                .awayDangerZones(new HashMap<>())
                .homePressureVector(new double[4])
                .awayPressureVector(new double[4])
                .totalAccumulatedXg(0.0)
                .lastUpdatedEpochMs(System.currentTimeMillis())
                .build();
    }

    public TacticalMatchDossier getDossier(String matchId) {
        return dossierCache.get(matchId);
    }

    public TacticalTensor getTensor(String matchId) {
        return tensorCache.get(matchId);
    }

    public Map<String, TacticalMatchDossier> getAllDossiers() {
        return dossierCache;
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("active_dossiers_count", dossierCache.size());
        metrics.put("active_tensors_count", tensorCache.size());
        metrics.put("cached_match_ids", dossierCache.keySet());
        return metrics;
    }
}
