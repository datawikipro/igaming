package pro.datawiki.igaming.source.apuestatotal.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(10)
public class ApuestatotalEsportsHandler extends AbstractApuestatotalMarketHandler {

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern NUMERIC_PARAM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^(?i)(?:handicap\\s*[12]|фора\\s*[12]|hándicap\\s*[12]|team\\s*[12]|команда\\s*[12]|equipo\\s*[12]|h[12]|ф[12]|[12])\\b\\s*");
    private static final Pattern TEAM1_PATTERN = Pattern.compile("(?i)(team\\s*1|home|local|casa|equipo\\s*1|команда\\s*1|1-я\\s*команда|1-й\\s*команд|первой\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?1|ит\\s*1|ит1)");
    private static final Pattern TEAM2_PATTERN = Pattern.compile("(?i)(team\\s*2|away|visitante|visita|fora|equipo\\s*2|команда\\s*2|2-я\\s*команда|2-й\\s*команд|второй\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?2|ит\\s*2|ит2)");

    private static final Pattern MAP_AFTER_KEYWORD = Pattern.compile("(?iU)(?:map|карт[аеыу]|mapa)\\s*#?\\s*([1-7])(?![0-9])");
    private static final Pattern MAP_BEFORE_KEYWORD = Pattern.compile("(?iU)(?:^|[^0-9a-zA-Z\\u0400-\\u04FF])([1-7])\\s*(?:-?[яйаое]|st|nd|rd|th|ª|º)?\\s*(?:map|карт[аеыу]|mapa)");
    private static final Pattern MAP_ORDINAL_WORD = Pattern.compile("(?iU)\\b(first|second|third|fourth|fifth|sixth|seventh|primer|primera|segundo|segunda|tercer|tercera|cuarto|cuarta|quinto|quinta|primeiro|primeira|terceiro|terceira|первая|первой|вторая|второй|третья|третьей|четвертая|четвертой|пятая|пятой)\\s*(?:map|mapa|карт[аеыу])");

    @Override
    public boolean supports(ApuestatotalStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }

        // Exclude other sports that have rounds (Boxing, MMA)
        if (sportType == SportType.BOXING || sportType == SportType.MMA) {
            return false;
        }

        // Non-esports sports should not be handled by esports handler
        if (sportType != null && !isEsports(sportType) && sportType != SportType.UNKNOWN) {
            return false;
        }

        Long id = group.getId();
        if (id != null) {
            // Known esports group IDs:
            // 703: Map 1 Winner, 704: Map 2 Winner, 705: Map 3 Winner
            // 740, 741: Map Handicap
            // 742: Map Total
            if (id == 703L || id == 704L || id == 705L || id == 740L || id == 741L || id == 742L) {
                return true;
            }
        }

        String name = getGroupName(group).toLowerCase();

        // Exclude stats/markets of other sports
        if (name.contains("corner") || name.contains("углов") || name.contains("córner") || name.contains("esquina")
                || name.contains("card") || name.contains("карточ") || name.contains("tarjeta") || name.contains("booking")
                || name.contains("foul") || name.contains("фол") || name.contains("falta")
                || name.contains("penalty") || name.contains("пенальти") || name.contains("penal")
                || name.contains("offside") || name.contains("офсайд") || name.contains("fuera de juego")) {
            return false;
        }

        // Check for map or round keywords
        boolean hasMap = hasMapKeyword(name);
        boolean hasRound = name.contains("round") || name.contains("раунд") || name.contains("ronda");

        if (hasMap || hasRound) {
            return true;
        }

        // If it's an esports sport, also support general match winner/handicap/total
        if (isEsports(sportType)) {
            if (id != null && (id == 1L || id == 702L || id == 2L || id == 3L)) {
                return true;
            }
            return name.contains("match result") || name.contains("1x2") || name.contains("moneyline")
                    || name.contains("winner") || name.contains("победитель") || name.contains("ganador")
                    || name.contains("исход") || name.contains("победа в матче")
                    || name.contains("handicap") || name.contains("фора") || name.contains("hándicap") || name.contains("spread") || name.contains("desventaja")
                    || name.contains("total") || name.contains("тотал") || name.contains("más/menos") || name.contains("over/under");
        }

        return false;
    }

    @Override
    public void handle(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return;
        }

        String lowerName = getGroupName(group).toLowerCase();
        Long id = group.getId();

        // 1. Map Winner markets (e.g. Map 1 Winner, Map 2 Winner, IDs 703, 704, 705)
        if ((id != null && (id == 703L || id == 704L || id == 705L)) || isMapWinnerMarket(group, lowerName)) {
            handleMapWinner(group, match, sportType, items);
            return;
        }

        // 2. Map Handicap markets (ID 740, 741 or "map handicap", "handicap maps", or general handicap in esports)
        if ((id != null && (id == 740L || id == 741L)) || isMapHandicapMarket(group, lowerName)
                || (isEsports(sportType) && isGeneralHandicap(group, lowerName))) {
            handleMapHandicap(group, match, sportType, items);
            return;
        }

        // 3. Map Total markets (ID 742 or "map total", "total maps", or general total in esports)
        if ((id != null && id == 742L) || isMapTotalMarket(group, lowerName)
                || (isEsports(sportType) && isGeneralTotal(group, lowerName))) {
            handleMapTotal(group, match, sportType, items);
            return;
        }

        // 4. Round Handicap within a specific Map
        if (isRoundHandicapMarket(group, lowerName)) {
            handleRoundHandicap(group, match, sportType, items);
            return;
        }

        // 5. Round Total within a specific Map
        if (isRoundTotalMarket(group, lowerName)) {
            handleRoundTotal(group, match, sportType, items);
            return;
        }

        // 6. Generic Map Total or Round Total
        if (lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("over/under") || lowerName.contains("más/menos")) {
            if (hasMapKeyword(lowerName)) {
                handleMapTotal(group, match, sportType, items);
            } else {
                handleRoundTotal(group, match, sportType, items);
            }
            return;
        }

        // 7. Generic Map Handicap or Round Handicap
        if (lowerName.contains("handicap") || lowerName.contains("фора") || lowerName.contains("hándicap") || lowerName.contains("spread") || lowerName.contains("desventaja")) {
            if (hasMapKeyword(lowerName)) {
                handleMapHandicap(group, match, sportType, items);
            } else {
                handleRoundHandicap(group, match, sportType, items);
            }
            return;
        }

        // 8. General match winner for Esports
        if (isEsports(sportType)) {
            handleMatchWinner(group, match, sportType, items);
        }
    }

    private void handleMapWinner(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveMapScope(group);
        String groupName = getGroupName(group);
        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);

        for (ApuestatotalStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            BetType betType = null;
            if (isTeam1ResultStake(stake, match)) {
                MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                betType = new MatchResultBet(scope, outcome, StatType.MATCH);
            } else if (isDrawStake(stake)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH);
            } else if (isTeam2ResultStake(stake, match)) {
                MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                betType = new MatchResultBet(scope, outcome, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, stake, groupName, betType, null);
            }
        }
    }

    private void handleMatchWinner(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);
        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);

        for (ApuestatotalStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            BetType betType = null;
            if (isTeam1ResultStake(stake, match)) {
                MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN1 : MatchResultBet.Outcome.WIN1_2WAY;
                betType = new MatchResultBet(scope, outcome, StatType.MATCH);
            } else if (isDrawStake(stake)) {
                betType = new MatchResultBet(scope, MatchResultBet.Outcome.DRAW, StatType.MATCH);
            } else if (isTeam2ResultStake(stake, match)) {
                MatchResultBet.Outcome outcome = hasDraw ? MatchResultBet.Outcome.WIN2 : MatchResultBet.Outcome.WIN2_2WAY;
                betType = new MatchResultBet(scope, outcome, StatType.MATCH);
            }

            if (betType != null) {
                addOddItem(items, stake, groupName, betType, null);
            }
        }
    }

    private void handleMapHandicap(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = BetScope.FULL_MATCH;
        String groupName = getGroupName(group);

        for (ApuestatotalStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double param = resolveHandicapParam(stake, group);
            if (param == null) continue;

            HandicapBet.Outcome outcome = resolveHandicapOutcome(stake, match);
            if (outcome == null) continue;

            boolean isAsian = isQuarterAsian(param);
            HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, StatType.MAPS);
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    private void handleMapTotal(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = BetScope.FULL_MATCH;
        String groupName = getGroupName(group);

        for (ApuestatotalStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double param = resolveTotalParam(stake, group);
            if (param == null || param < 0) continue;

            TotalBet.Direction direction = resolveTotalDirection(stake);
            if (direction == null) continue;

            BetSubject subject = resolveTotalSubject(group, stake, match);
            boolean isAsian = isQuarterAsian(param);

            TotalBet betType = new TotalBet(scope, subject, direction, param, isAsian, StatType.MAPS);
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    private void handleRoundHandicap(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveMapScope(group);
        String groupName = getGroupName(group);

        for (ApuestatotalStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double param = resolveHandicapParam(stake, group);
            if (param == null) continue;

            HandicapBet.Outcome outcome = resolveHandicapOutcome(stake, match);
            if (outcome == null) continue;

            boolean isAsian = isQuarterAsian(param);
            HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, StatType.ROUNDS);
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    private void handleRoundTotal(ApuestatotalStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveMapScope(group);
        String groupName = getGroupName(group);

        for (ApuestatotalStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double param = resolveTotalParam(stake, group);
            if (param == null || param < 0) continue;

            TotalBet.Direction direction = resolveTotalDirection(stake);
            if (direction == null) continue;

            BetSubject subject = resolveTotalSubject(group, stake, match);
            boolean isAsian = isQuarterAsian(param);

            TotalBet betType = new TotalBet(scope, subject, direction, param, isAsian, StatType.ROUNDS);
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    private boolean isMapWinnerMarket(ApuestatotalStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && (id == 703L || id == 704L || id == 705L)) return true;
        if (lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("handicap")
                || lowerName.contains("hándicap") || lowerName.contains("фора") || lowerName.contains("spread")
                || lowerName.contains("desventaja") || lowerName.contains("round") || lowerName.contains("раунд")
                || lowerName.contains("ronda")) {
            return false;
        }
        return hasMapKeyword(lowerName);
    }

    private boolean isMapHandicapMarket(ApuestatotalStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && (id == 740L || id == 741L)) return true;
        if (lowerName.contains("round") || lowerName.contains("раунд") || lowerName.contains("ronda")) return false;
        return (lowerName.contains("handicap") || lowerName.contains("фора") || lowerName.contains("hándicap") || lowerName.contains("spread") || lowerName.contains("desventaja"))
                && (lowerName.contains("map") || lowerName.contains("карт") || lowerName.contains("mapa"));
    }

    private boolean isMapTotalMarket(ApuestatotalStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && id == 742L) return true;
        if (lowerName.contains("round") || lowerName.contains("раунд") || lowerName.contains("ronda")) return false;
        return (lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("over/under") || lowerName.contains("más/menos"))
                && (lowerName.contains("map") || lowerName.contains("карт") || lowerName.contains("mapa"));
    }

    private boolean isGeneralHandicap(ApuestatotalStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && (id == 2L || id == 740L || id == 741L)) return true;
        if (lowerName.contains("round") || lowerName.contains("раунд") || lowerName.contains("ronda")) return false;
        return lowerName.contains("handicap") || lowerName.contains("фора") || lowerName.contains("hándicap") || lowerName.contains("spread") || lowerName.contains("desventaja");
    }

    private boolean isGeneralTotal(ApuestatotalStakeGroupData group, String lowerName) {
        Long id = group.getId();
        if (id != null && (id == 3L || id == 742L)) return true;
        if (lowerName.contains("round") || lowerName.contains("раунд") || lowerName.contains("ronda")) return false;
        return lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("over/under") || lowerName.contains("más/menos");
    }

    private boolean isRoundHandicapMarket(ApuestatotalStakeGroupData group, String lowerName) {
        return (lowerName.contains("handicap") || lowerName.contains("фора") || lowerName.contains("hándicap") || lowerName.contains("spread") || lowerName.contains("desventaja"))
                && (lowerName.contains("round") || lowerName.contains("раунд") || lowerName.contains("ronda"));
    }

    private boolean isRoundTotalMarket(ApuestatotalStakeGroupData group, String lowerName) {
        return (lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("over/under") || lowerName.contains("más/menos"))
                && (lowerName.contains("round") || lowerName.contains("раунд") || lowerName.contains("ronda"));
    }

    private boolean hasMapKeyword(String lowerName) {
        return MAP_AFTER_KEYWORD.matcher(lowerName).find()
                || MAP_BEFORE_KEYWORD.matcher(lowerName).find()
                || MAP_ORDINAL_WORD.matcher(lowerName).find()
                || lowerName.contains("карта") || lowerName.contains("карте") || lowerName.contains("карты")
                || lowerName.contains("map 1") || lowerName.contains("map 2") || lowerName.contains("map 3")
                || lowerName.contains("map 4") || lowerName.contains("map 5")
                || lowerName.contains("mapa 1") || lowerName.contains("mapa 2") || lowerName.contains("mapa 3")
                || lowerName.contains("mapa 4") || lowerName.contains("mapa 5");
    }

    public static boolean isEsports(SportType sportType) {
        if (sportType == null) return false;
        return switch (sportType) {
            case ESPORTS, CS2, DOTA2, LEAGUE_OF_LEGENDS, VALORANT, STARCRAFT,
                 FIGHTING_GAMES, MOBILE_LEGENDS, CROSSFIRE, RAINBOW_SIX,
                 ROCKET_LEAGUE, CALL_OF_DUTY, OVERWATCH, PUBG -> true;
            default -> false;
        };
    }

    private BetScope resolveMapScope(ApuestatotalStakeGroupData group) {
        Long id = group.getId();
        if (id != null) {
            if (id == 703L) return BetScope.MAP_1;
            if (id == 704L) return BetScope.MAP_2;
            if (id == 705L) return BetScope.MAP_3;
        }

        String name = getGroupName(group);

        Matcher after = MAP_AFTER_KEYWORD.matcher(name);
        if (after.find()) {
            return mapNumberToScope(Integer.parseInt(after.group(1)));
        }

        Matcher before = MAP_BEFORE_KEYWORD.matcher(name);
        if (before.find()) {
            return mapNumberToScope(Integer.parseInt(before.group(1)));
        }

        Matcher ordinal = MAP_ORDINAL_WORD.matcher(name);
        if (ordinal.find()) {
            String word = ordinal.group(1).toLowerCase();
            return switch (word) {
                case "first", "primer", "primera", "primeiro", "primeira", "первая", "первой" -> BetScope.MAP_1;
                case "second", "segundo", "segunda", "вторая", "второй" -> BetScope.MAP_2;
                case "third", "tercer", "tercera", "terceiro", "terceira", "третья", "третьей" -> BetScope.MAP_3;
                case "fourth", "cuarto", "cuarta", "четвертая", "четвертой" -> BetScope.MAP_4;
                case "fifth", "quinto", "quinta", "пятая", "пятой" -> BetScope.MAP_5;
                default -> BetScope.FULL_MATCH;
            };
        }

        return BetScope.FULL_MATCH;
    }

    private BetScope mapNumberToScope(int mapNumber) {
        return switch (mapNumber) {
            case 1 -> BetScope.MAP_1;
            case 2 -> BetScope.MAP_2;
            case 3 -> BetScope.MAP_3;
            case 4 -> BetScope.MAP_4;
            case 5 -> BetScope.MAP_5;
            default -> BetScope.FULL_MATCH;
        };
    }

    private boolean isDrawStake(ApuestatotalStakeData s) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim().toUpperCase() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim().toUpperCase() : "";
        return "X".equals(en) || "X".equals(ru) || "Х".equals(en) || "Х".equals(ru)
                || "DRAW".equals(en) || "НИЧЬЯ".equals(ru) || "EMPATE".equals(en)
                || en.contains("DRAW") || ru.contains("НИЧЬЯ") || en.contains("EMPATE");
    }

    private boolean isTeam1ResultStake(ApuestatotalStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("1".equalsIgnoreCase(en) || "1".equalsIgnoreCase(ru)
                || "Win1".equalsIgnoreCase(en) || "Win1".equalsIgnoreCase(ru)
                || "W1".equalsIgnoreCase(en) || "W1".equalsIgnoreCase(ru)
                || "П1".equalsIgnoreCase(ru) || "П1".equalsIgnoreCase(en)
                || "P1".equalsIgnoreCase(en) || "P1".equalsIgnoreCase(ru)
                || "Home".equalsIgnoreCase(en) || "Home".equalsIgnoreCase(ru)
                || "Local".equalsIgnoreCase(en) || "Local".equalsIgnoreCase(ru)
                || "Casa".equalsIgnoreCase(en) || "Casa".equalsIgnoreCase(ru)
                || "Победа 1".equalsIgnoreCase(ru) || "Победа 1".equalsIgnoreCase(en) || "Победа1".equalsIgnoreCase(ru)
                || "Team 1".equalsIgnoreCase(en) || "Team 1".equalsIgnoreCase(ru)
                || "Equipo 1".equalsIgnoreCase(en) || "Equipo 1".equalsIgnoreCase(ru)
                || "Victoria 1".equalsIgnoreCase(en) || "Victoria Local".equalsIgnoreCase(en)
                || "Gana 1".equalsIgnoreCase(en) || "Gana Local".equalsIgnoreCase(en)
                || en.startsWith("1 ") || ru.startsWith("1 ")
                || en.startsWith("1 -") || ru.startsWith("1 -")) {
            return true;
        }
        if (match != null && match.getTeam1() != null && !match.getTeam1().isBlank()) {
            String t1 = match.getTeam1().trim();
            if (en.equalsIgnoreCase(t1) || ru.equalsIgnoreCase(t1)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTeam2ResultStake(ApuestatotalStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("2".equalsIgnoreCase(en) || "2".equalsIgnoreCase(ru)
                || "Win2".equalsIgnoreCase(en) || "Win2".equalsIgnoreCase(ru)
                || "W2".equalsIgnoreCase(en) || "W2".equalsIgnoreCase(ru)
                || "П2".equalsIgnoreCase(ru) || "П2".equalsIgnoreCase(en)
                || "P2".equalsIgnoreCase(en) || "P2".equalsIgnoreCase(ru)
                || "Away".equalsIgnoreCase(en) || "Away".equalsIgnoreCase(ru)
                || "Visitante".equalsIgnoreCase(en) || "Visitante".equalsIgnoreCase(ru)
                || "Visita".equalsIgnoreCase(en) || "Visita".equalsIgnoreCase(ru)
                || "Fora".equalsIgnoreCase(en) || "Fora".equalsIgnoreCase(ru)
                || "Победа 2".equalsIgnoreCase(ru) || "Победа 2".equalsIgnoreCase(en) || "Победа2".equalsIgnoreCase(ru)
                || "Team 2".equalsIgnoreCase(en) || "Team 2".equalsIgnoreCase(ru)
                || "Equipo 2".equalsIgnoreCase(en) || "Equipo 2".equalsIgnoreCase(ru)
                || "Victoria 2".equalsIgnoreCase(en) || "Victoria Visitante".equalsIgnoreCase(en)
                || "Gana 2".equalsIgnoreCase(en) || "Gana Visita".equalsIgnoreCase(en) || "Gana Visitante".equalsIgnoreCase(en)
                || en.startsWith("2 ") || ru.startsWith("2 ")
                || en.startsWith("2 -") || ru.startsWith("2 -")) {
            return true;
        }
        if (match != null && match.getTeam2() != null && !match.getTeam2().isBlank()) {
            String t2 = match.getTeam2().trim();
            if (en.equalsIgnoreCase(t2) || ru.equalsIgnoreCase(t2)) {
                return true;
            }
        }
        return false;
    }

    private HandicapBet.Outcome resolveHandicapOutcome(ApuestatotalStakeData stake, MatchCache match) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
        String upperEn = en.toUpperCase();
        String upperRu = ru.toUpperCase();

        if (upperEn.equals("X") || upperRu.equals("X") || upperRu.equals("Х")
                || upperEn.equals("DRAW") || upperRu.equals("НИЧЬЯ") || upperEn.equals("EMPATE")
                || upperEn.startsWith("DRAW") || upperRu.startsWith("НИЧЬЯ") || upperEn.startsWith("EMPATE")
                || upperRu.startsWith("ФОРА Х") || upperRu.startsWith("ФОРА X")) {
            return HandicapBet.Outcome.DRAW;
        }

        if (upperEn.equals("1") || upperEn.equals("H1") || upperRu.equals("Ф1") || upperRu.equals("ФОРА 1") || upperRu.equals("ФОРА1")
                || upperEn.equals("HANDICAP 1") || upperEn.equals("HÁNDICAP 1") || upperEn.equals("HOME") || upperEn.equals("LOCAL") || upperEn.equals("CASA")
                || upperEn.startsWith("1 ") || upperRu.startsWith("1 ")
                || upperEn.startsWith("H1 ") || upperRu.startsWith("Ф1 ") || upperRu.startsWith("Ф1(")
                || upperEn.startsWith("HANDICAP 1") || upperEn.startsWith("HÁNDICAP 1") || upperRu.startsWith("ФОРА 1") || upperRu.startsWith("ФОРА1")
                || upperEn.startsWith("HOME") || upperEn.startsWith("LOCAL") || upperEn.startsWith("CASA")) {
            return HandicapBet.Outcome.TEAM1;
        }

        if (upperEn.equals("2") || upperEn.equals("H2") || upperRu.equals("Ф2") || upperRu.equals("ФОРА 2") || upperRu.equals("ФОРА2")
                || upperEn.equals("HANDICAP 2") || upperEn.equals("HÁNDICAP 2") || upperEn.equals("AWAY") || upperEn.equals("VISITANTE") || upperEn.equals("VISITA") || upperEn.equals("FORA")
                || upperEn.startsWith("2 ") || upperRu.startsWith("2 ")
                || upperEn.startsWith("H2 ") || upperRu.startsWith("Ф2 ") || upperRu.startsWith("Ф2(")
                || upperEn.startsWith("HANDICAP 2") || upperEn.startsWith("HÁNDICAP 2") || upperRu.startsWith("ФОРА 2") || upperRu.startsWith("ФОРА2")
                || upperEn.startsWith("AWAY") || upperEn.startsWith("VISITANTE") || upperEn.startsWith("VISITA") || upperEn.startsWith("FORA")) {
            return HandicapBet.Outcome.TEAM2;
        }

        if (match != null) {
            if (match.getTeam1() != null && !match.getTeam1().isBlank()) {
                String t1 = match.getTeam1().trim();
                if (en.equalsIgnoreCase(t1) || ru.equalsIgnoreCase(t1)
                        || upperEn.startsWith(t1.toUpperCase()) || upperRu.startsWith(t1.toUpperCase())) {
                    return HandicapBet.Outcome.TEAM1;
                }
            }
            if (match.getTeam2() != null && !match.getTeam2().isBlank()) {
                String t2 = match.getTeam2().trim();
                if (en.equalsIgnoreCase(t2) || ru.equalsIgnoreCase(t2)
                        || upperEn.startsWith(t2.toUpperCase()) || upperRu.startsWith(t2.toUpperCase())) {
                    return HandicapBet.Outcome.TEAM2;
                }
            }
        }

        return null;
    }

    private TotalBet.Direction resolveTotalDirection(ApuestatotalStakeData stake) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim().toUpperCase() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim().toUpperCase() : "";

        if (en.equals("OVER") || ru.equals("БОЛЬШЕ") || ru.equals("Б") || en.equals("O")
                || en.equals("MÁS") || en.equals("MAS") || en.equals("MAIS")
                || en.startsWith("OVER") || ru.startsWith("БОЛЬШЕ") || ru.startsWith("ТБ")
                || en.startsWith("MÁS DE") || en.startsWith("MAS DE") || en.startsWith("MAIS DE")) {
            return TotalBet.Direction.OVER;
        }

        if (en.equals("UNDER") || ru.equals("МЕНЬШЕ") || ru.equals("М") || en.equals("U")
                || en.equals("MENOS")
                || en.startsWith("UNDER") || ru.startsWith("МЕНЬШЕ") || ru.startsWith("ТМ")
                || en.startsWith("MENOS DE")) {
            return TotalBet.Direction.UNDER;
        }

        return null;
    }

    private BetSubject resolveTotalSubject(ApuestatotalStakeGroupData group, ApuestatotalStakeData stake, MatchCache match) {
        String stakeEn = stake.getNameEn() != null ? stake.getNameEn() : "";
        String stakeRu = stake.getNameRu() != null ? stake.getNameRu() : "";
        String stakeCombined = stakeEn + " " + stakeRu;

        boolean stakeT1 = TEAM1_PATTERN.matcher(stakeCombined).find();
        boolean stakeT2 = TEAM2_PATTERN.matcher(stakeCombined).find();

        if (stakeT1 && !stakeT2) return BetSubject.TEAM1;
        if (stakeT2 && !stakeT1) return BetSubject.TEAM2;

        return BetSubject.MATCH;
    }

    private Double resolveTotalParam(ApuestatotalStakeData stake, ApuestatotalStakeGroupData group) {
        if (stake.getArgument() != null) return stake.getArgument();
        Double parsed = extractNumericParam(stake.getNameEn());
        if (parsed == null) parsed = extractNumericParam(stake.getNameRu());
        if (parsed == null) parsed = extractNumericParam(getGroupName(group));
        return parsed;
    }

    private Double resolveHandicapParam(ApuestatotalStakeData stake, ApuestatotalStakeGroupData group) {
        Double param = stake.getArgument();
        String name = ((stake.getNameEn() != null ? stake.getNameEn() : "") + " "
                + (stake.getNameRu() != null ? stake.getNameRu() : "")).trim();

        Double parsedFromName = extractSignedNumericParam(name);
        if (parsedFromName != null) {
            if (param != null && param > 0 && parsedFromName < 0 && Math.abs(param - Math.abs(parsedFromName)) < 0.001) {
                return parsedFromName;
            }
            if (param == null) return parsedFromName;
        }
        if (param == null) {
            return extractSignedNumericParam(getGroupName(group));
        }
        return param;
    }

    private Double extractNumericParam(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher parenMatcher = PAREN_PARAM_PATTERN.matcher(text);
        if (parenMatcher.find()) {
            try { return Double.parseDouble(parenMatcher.group(1)); } catch (NumberFormatException ignored) {}
        }
        String stripped = PREFIX_PATTERN.matcher(text).replaceFirst("");
        Matcher numMatcher = NUMERIC_PARAM_PATTERN.matcher(stripped);
        if (numMatcher.find()) {
            try { return Double.parseDouble(numMatcher.group(1)); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Double extractSignedNumericParam(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher parenMatcher = PAREN_PARAM_PATTERN.matcher(text);
        if (parenMatcher.find()) {
            try { return Double.parseDouble(parenMatcher.group(1)); } catch (NumberFormatException ignored) {}
        }
        String stripped = PREFIX_PATTERN.matcher(text).replaceFirst("");
        Matcher numMatcher = NUMERIC_PARAM_PATTERN.matcher(stripped);
        if (numMatcher.find()) {
            try { return Double.parseDouble(numMatcher.group(1)); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private boolean isQuarterAsian(Double param) {
        if (param == null) return false;
        return Math.abs(param * 4 - Math.round(param * 4)) < 0.001
                && Math.abs(param * 2 - Math.round(param * 2)) > 0.001;
    }
}
