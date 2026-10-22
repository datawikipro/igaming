package pro.datawiki.igaming.source.fanduel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.service.BetTypeResolverService;
import pro.datawiki.igaming.source.core.service.SportNormalizationService;
import pro.datawiki.igaming.source.core.service.UnmappedBetService;
import pro.datawiki.igaming.source.fanduel.dto.FanDuelEventGroupResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FanDuelOddsMapper {

    private final UnmappedBetService unmappedBetService;
    private final SportNormalizationService sportNormalizationService;
    private final BetTypeResolverService betTypeResolver;

    public OddsUpdateRequest mapToOddsUpdateRequest(FanDuelEventGroupResponse.FanDuelEvent event,
                                                    FanDuelEventGroupResponse response,
                                                    String sportName,
                                                    String leagueName) {
        if (event == null) return null;

        OddsUpdateRequest request = new OddsUpdateRequest();
        request.setBookmaker("fanduel");
        request.setExternalEventId(String.valueOf(event.getEventId()));

        if (event.getOpenDate() != null) {
            try {
                request.setStartTime(Instant.parse(event.getOpenDate()).toEpochMilli());
            } catch (Exception e) {
                log.warn("Failed to parse start date '{}'", event.getOpenDate());
            }
        }

        request.setSportName(sportName);
        SportType sportType = sportNormalizationService.normalize(sportName);
        request.setSportType(sportType);
        request.setLeagueName(leagueName);

        String team1 = null;
        String team2 = null;
        if (event.getName() != null) {
            String[] parts = event.getName().split("(?i)\\s+(?:@|vs\\.?|v|-|–|—|/)\\s+");
            if (parts.length == 2) {
                team1 = parts[0].trim();
                team2 = parts[1].trim();
            } else {
                parts = event.getName().split("(?i)\\s*[/–—-]\\s*");
                if (parts.length == 2) {
                    team1 = parts[0].trim();
                    team2 = parts[1].trim();
                }
            }
        }

        // Fallback: extract team1 and team2 from runners or selections of a 2-way head-to-head market if name parsing failed
        if ((team1 == null || team2 == null) && response != null && response.getAttachments() != null && response.getAttachments().getMarkets() != null) {
            for (FanDuelEventGroupResponse.FanDuelMarket market : response.getAttachments().getMarkets().values()) {
                if (market != null && market.getEventId() != null && market.getEventId().equals(event.getEventId())) {
                    if (market.getRunners() != null && market.getRunners().size() == 2) {
                        String r1 = market.getRunners().get(0).getRunnerName();
                        String r2 = market.getRunners().get(1).getRunnerName();
                        if (r1 != null && r2 != null && !r1.equalsIgnoreCase("Over") && !r1.equalsIgnoreCase("Under") && !r1.equalsIgnoreCase("Yes") && !r1.equalsIgnoreCase("No") && !r1.equalsIgnoreCase("Draw") && !r1.equalsIgnoreCase("Tie")) {
                            team1 = r1.trim();
                            team2 = r2.trim();
                            break;
                        }
                    } else if (response.getAttachments().getSelections() != null) {
                        List<String> sels = new ArrayList<>();
                        for (FanDuelEventGroupResponse.FanDuelSelection sel : response.getAttachments().getSelections().values()) {
                            if (market.getMarketId() != null && market.getMarketId().equals(sel.getMarketId())) {
                                String sName = sel.getName();
                                if (sName != null && !sName.equalsIgnoreCase("Over") && !sName.equalsIgnoreCase("Under") && !sName.equalsIgnoreCase("Yes") && !sName.equalsIgnoreCase("No") && !sName.equalsIgnoreCase("Draw") && !sName.equalsIgnoreCase("Tie")) {
                                    sels.add(sName.trim());
                                }
                            }
                        }
                        if (sels.size() == 2) {
                            team1 = sels.get(0);
                            team2 = sels.get(1);
                            break;
                        }
                    }
                }
            }
        }
        request.setTeam1(team1);
        request.setTeam2(team2);

        request.setIsLive(false);
        request.setEventUrl("https://sportsbook.fanduel.com/event/" + event.getEventId());

        List<OddItem> oddsList = new ArrayList<>();
        if (response.getAttachments() != null) {
            FanDuelEventGroupResponse.Attachments att = response.getAttachments();
            if (att.getMarkets() != null) {
                for (FanDuelEventGroupResponse.FanDuelMarket market : att.getMarkets().values()) {
                    if (market.getEventId() != null && market.getEventId().equals(event.getEventId())) {
                        if (market.getRunners() != null && !market.getRunners().isEmpty()) {
                            for (FanDuelEventGroupResponse.FanDuelRunner runner : market.getRunners()) {
                                processRunnerOutcome(event, market.getMarketName(), market, runner, sportType, sportName, team1, team2, oddsList);
                            }
                        } else if (att.getSelections() != null) {
                            for (FanDuelEventGroupResponse.FanDuelSelection selection : att.getSelections().values()) {
                                if (market.getMarketId().equals(selection.getMarketId())) {
                                    processOutcome(event, market.getMarketName(), market, selection, sportType, sportName, team1, team2, oddsList);
                                }
                            }
                        }
                    }
                }
            }
        }
        request.setOdds(oddsList);
        return request;
    }


    private void processOutcome(FanDuelEventGroupResponse.FanDuelEvent event,
                                String marketName,
                                FanDuelEventGroupResponse.FanDuelMarket market,
                                FanDuelEventGroupResponse.FanDuelSelection selection,
                                SportType sportType,
                                String sportName,
                                String team1,
                                String team2,
                                List<OddItem> oddsList) {
        
        Double decimalOdds = selection.getTrueOdds();
        if (decimalOdds == null && selection.getPrice() != null) {
            decimalOdds = selection.getPrice().getDecimal();
            if (decimalOdds == null && selection.getPrice().getAmerican() != null) {
                int am = selection.getPrice().getAmerican();
                if (am > 0) decimalOdds = (am / 100.0) + 1.0;
                else if (am < 0) decimalOdds = (100.0 / Math.abs(am)) + 1.0;
            }
        }
        if (decimalOdds == null || decimalOdds <= 1.0) return;

        String runnerName = selection.getName();
        if (runnerName == null) runnerName = "Outcome " + selection.hashCode();

        BetType betType = resolveBetType(marketName, selection, sportType, team1, team2);

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            logUnmapped(event, sportName, marketName, runnerName);
            return;
        }

        OddItem item = new OddItem();
        item.setFactorId(market.getMarketId() + "_" + selection.getSelectionId());
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(decimalOdds);
        item.setBetType(betType);

        oddsList.add(item);
    }

    private void processRunnerOutcome(FanDuelEventGroupResponse.FanDuelEvent event,
                                      String marketName,
                                      FanDuelEventGroupResponse.FanDuelMarket market,
                                      FanDuelEventGroupResponse.FanDuelRunner runner,
                                      SportType sportType,
                                      String sportName,
                                      String team1,
                                      String team2,
                                      List<OddItem> oddsList) {
        if (runner == null || "SUSPENDED".equalsIgnoreCase(runner.getRunnerStatus())) return;

        Double decimalOdds = null;
        if (runner.getWinRunnerOdds() != null) {
            if (runner.getWinRunnerOdds().getTrueOdds() != null &&
                runner.getWinRunnerOdds().getTrueOdds().getDecimalOdds() != null) {
                decimalOdds = runner.getWinRunnerOdds().getTrueOdds().getDecimalOdds().getDecimalOdds();
            }
            if (decimalOdds == null && runner.getWinRunnerOdds().getAmericanDisplayOdds() != null) {
                Integer am = runner.getWinRunnerOdds().getAmericanDisplayOdds().getAmericanOddsInt();
                if (am == null) am = runner.getWinRunnerOdds().getAmericanDisplayOdds().getAmericanOdds();
                if (am != null && am != 0) {
                    decimalOdds = am > 0 ? (am / 100.0) + 1.0 : (100.0 / Math.abs(am)) + 1.0;
                }
            }
        }
        if (decimalOdds == null || decimalOdds <= 1.0) return;

        String runnerName = runner.getRunnerName();
        if (runnerName == null) runnerName = "Outcome " + runner.getSelectionId();

        BetType betType = resolveBetType(marketName, runnerName, runner.getHandicap(), sportType, team1, team2);

        if (betType == null || "UNKNOWN".equals(betType.code())) {
            logUnmapped(event, sportName, marketName, runnerName);
            return;
        }

        OddItem item = new OddItem();
        item.setFactorId(market.getMarketId() + "_" + runner.getSelectionId());
        item.setGroupName(marketName);
        item.setName(runnerName);
        item.setValue(decimalOdds);
        item.setBetType(betType);

        oddsList.add(item);
    }

    private BetType resolveBetType(String marketName,
                                   FanDuelEventGroupResponse.FanDuelSelection selection,
                                   SportType sportType,
                                   String team1,
                                   String team2) {
        String runnerName = selection != null ? selection.getName() : "";
        Double handicap = selection != null ? selection.getHandicap() : null;
        return resolveBetType(marketName, runnerName, handicap, sportType, team1, team2);
    }

    private BetType resolveBetType(String marketName,
                                   String runnerName,
                                   Double runnerHandicap,
                                   SportType sportType,
                                   String team1,
                                   String team2) {
        if (marketName == null) return null;
        String mUpper = marketName.toUpperCase();
        if (runnerName == null) runnerName = "";

        Double line = runnerHandicap != null ? runnerHandicap : 0.0;
        if (line == 0.0 && runnerName.contains(" ") && runnerName.matches(".*\\d+\\.?\\d*.*")) {
             String[] parts = runnerName.split(" ");
             try {
                 line = Double.parseDouble(parts[parts.length - 1].replace("+", ""));
             } catch (Exception ignored) {}
        }

        // 1. Result Markets (Moneyline)
        if (mUpper.contains("MONEYLINE") || mUpper.contains("MATCH RESULT") || mUpper.contains("3-WAY") || mUpper.contains("WIN-DRAW-WIN")) {
            boolean is3Way = mUpper.contains("3-WAY") || mUpper.contains("WIN-DRAW-WIN") || sportType == SportType.FOOTBALL;
            if (team1 != null && (runnerName.equalsIgnoreCase(team1) || runnerName.toUpperCase().contains(team1.toUpperCase()))) {
                return new MatchResultBet(BetScope.FULL_MATCH, is3Way ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY, null);
            } else if (team2 != null && (runnerName.equalsIgnoreCase(team2) || runnerName.toUpperCase().contains(team2.toUpperCase()))) {
                return new MatchResultBet(BetScope.FULL_MATCH, is3Way ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY, null);
            } else if (runnerName.toUpperCase().contains("DRAW") || runnerName.toUpperCase().contains("TIE")) {
                return new MatchResultBet(BetScope.FULL_MATCH, MatchResultBet.Outcome.DRAW, null);
            }
        }


        // 2. Totals Markets (Over/Under)
        if (mUpper.contains("TOTAL") || mUpper.contains("OVER/UNDER") || mUpper.contains("O/U")) {
            if (runnerName.toUpperCase().startsWith("O ") || runnerName.toUpperCase().startsWith("OVER")) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.OVER, line, false, null);
            } else if (runnerName.toUpperCase().startsWith("U ") || runnerName.toUpperCase().startsWith("UNDER")) {
                return new TotalBet(BetScope.FULL_MATCH, BetSubject.MATCH, TotalBet.Direction.UNDER, line, false, null);
            }
        }

        // 3. Spreads/Handicaps
        if (mUpper.contains("SPREAD") || mUpper.contains("HANDICAP") || mUpper.contains("RUN LINE") || mUpper.contains("PUCK LINE")) {
            if (team1 != null && runnerName.toUpperCase().contains(team1.toUpperCase())) {
                return new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM1, line, false, null);
            } else if (team2 != null && runnerName.toUpperCase().contains(team2.toUpperCase())) {
                return new HandicapBet(BetScope.FULL_MATCH, HandicapBet.Outcome.TEAM2, line, false, null);
            }
        }

        return betTypeResolver.resolve("fanduel", sportType, mUpper, runnerName.toUpperCase(), line);
    }


    private void logUnmapped(FanDuelEventGroupResponse.FanDuelEvent event, String sportName, String marketName, String runnerName) {
        log.debug("UNMAPPED FANDUEL MARKET: Event={}, Sport={}, Market={}, Runner={}",
                event.getEventId(), sportName, marketName, runnerName);
        unmappedBetService.saveAndNotify("fanduel", sportName, runnerName, marketName, event.getEventId().toString());
    }
}
