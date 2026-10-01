package pro.datawiki.igaming.source.vaidebet.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.dto.market.BetSubject;
import pro.datawiki.igaming.dto.market.HandicapBet;
import pro.datawiki.igaming.dto.market.MatchResultBet;
import pro.datawiki.igaming.dto.market.StatType;
import pro.datawiki.igaming.dto.market.TotalBet;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeData;
import pro.datawiki.igaming.source.vaidebet.dto.VaidebetStakeGroupData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(10)
public class VaidebetEsportsHandler extends AbstractVaidebetMarketHandler {

    private static final Pattern PAREN_PARAM_PATTERN = Pattern.compile("\\(([+-]?\\d+(?:\\.\\d+)?)\\)");
    private static final Pattern NUMERIC_PARAM_PATTERN = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^(?i)(?:handicap\\s*[12]|фора\\s*[12]|team\\s*[12]|команда\\s*[12]|h[12]|ф[12]|[12])\\b\\s*");
    private static final Pattern TEAM1_PATTERN = Pattern.compile("(?i)(team\\s*1|home|команда\\s*1|1-я\\s*команда|1-й\\s*команд|первой\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?1|ит\\s*1|ит1)");
    private static final Pattern TEAM2_PATTERN = Pattern.compile("(?i)(team\\s*2|away|команда\\s*2|2-я\\s*команда|2-й\\s*команд|второй\\s*команд|инд(?:ивидуальный)?\\s*тотал\\s*(?:команды\\s*)?2|ит\\s*2|ит2)");

    private static final Pattern MAP_AFTER_KEYWORD = Pattern.compile("(?iU)(?:map|карт[аеыу]|mapa)\\s*#?\\s*([1-7])(?![0-9])");
    private static final Pattern MAP_BEFORE_KEYWORD = Pattern.compile("(?iU)(?:^|[^0-9a-zA-Z\\u0400-\\u04FF])([1-7])\\s*(?:-?[яйаое]|st|nd|rd|th|ª|º)?\\s*(?:map|карт[аеыу]|mapa)");
    private static final Pattern MAP_ORDINAL_WORD = Pattern.compile("(?iU)\\b(first|second|third|fourth|fifth|sixth|seventh|primeiro|segundo|terceiro)\\s*(?:map|mapa)");

    @Override
    public boolean supports(VaidebetStakeGroupData group, SportType sportType) {
        if (group == null || group.getStakes() == null || group.getStakes().isEmpty()) {
            return false;
        }

        // Exclude other sports that have rounds (Boxing, MMA)
        if (sportType == SportType.BOXING || sportType == SportType.MMA) {
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
        if (name.contains("corner") || name.contains("углов") || name.contains("escanteio")
                || name.contains("card") || name.contains("карточ") || name.contains("cartao") || name.contains("booking")
                || name.contains("foul") || name.contains("фол")
                || name.contains("penalty") || name.contains("пенальти")
                || name.contains("offside") || name.contains("офсайд")) {
            return false;
        }

        // Check for map or round keywords
        boolean hasMap = hasMapKeyword(name);
        boolean hasRound = name.contains("round") || name.contains("раунд");

        if (hasMap || hasRound) {
            return true;
        }

        // If it's an esports sport, also support general match winner/handicap/total
        if (isEsports(sportType)) {
            if (id != null && (id == 1L || id == 702L)) {
                return true;
            }
            return name.contains("match result") || name.contains("1x2") || name.contains("moneyline")
                    || name.contains("winner") || name.contains("победитель") || name.contains("vencedor")
                    || name.contains("исход") || name.contains("победа в матче");
        }

        return false;
    }

    @Override
    public void handle(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
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

        // 2. Map Handicap markets (ID 740, 741 or "map handicap", "handicap maps")
        if ((id != null && (id == 740L || id == 741L)) || isMapHandicapMarket(group, lowerName)) {
            handleMapHandicap(group, match, sportType, items);
            return;
        }

        // 3. Map Total markets (ID 742 or "map total", "total maps")
        if ((id != null && id == 742L) || isMapTotalMarket(group, lowerName)) {
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

        // 6. Generic Esports Handicap (Match scope)
        if (lowerName.contains("handicap") || lowerName.contains("фора")) {
            handleGenericEsportsHandicap(group, match, sportType, items);
            return;
        }

        // 7. Generic Esports Total (Match scope)
        if (lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("over/under") || lowerName.contains("o/u")) {
            handleGenericEsportsTotal(group, match, sportType, items);
            return;
        }

        // 8. Generic Esports Match Winner
        handleGenericEsportsWinner(group, match, sportType, items);
    }

    private boolean isMapWinnerMarket(VaidebetStakeGroupData group, String lowerName) {
        boolean hasMap = hasMapKeyword(lowerName);
        boolean hasRound = lowerName.contains("round") || lowerName.contains("раунд");
        boolean hasHandicap = lowerName.contains("handicap") || lowerName.contains("фора");
        boolean hasTotal = lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("over/under") || lowerName.contains("o/u");

        return hasMap && !hasRound && !hasHandicap && !hasTotal;
    }

    private boolean isMapHandicapMarket(VaidebetStakeGroupData group, String lowerName) {
        boolean hasHandicap = lowerName.contains("handicap") || lowerName.contains("фора");
        boolean hasMapWord = lowerName.contains("map") || lowerName.contains("карт") || lowerName.contains("mapa");
        boolean hasRound = lowerName.contains("round") || lowerName.contains("раунд");

        if (!hasHandicap || hasRound) return false;

        return lowerName.contains("maps handicap") || lowerName.contains("map handicap")
                || lowerName.contains("handicap maps") || lowerName.contains("handicap map")
                || lowerName.contains("фора по картам") || lowerName.contains("фора карт")
                || (hasMapWord && extractMapNumber(lowerName) == null);
    }

    private boolean isMapTotalMarket(VaidebetStakeGroupData group, String lowerName) {
        boolean hasTotal = lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("over/under") || lowerName.contains("o/u");
        boolean hasMapWord = lowerName.contains("map") || lowerName.contains("карт") || lowerName.contains("mapa");
        boolean hasRound = lowerName.contains("round") || lowerName.contains("раунд");

        if (!hasTotal || hasRound) return false;

        return lowerName.contains("total maps") || lowerName.contains("maps total")
                || lowerName.contains("тотал карт") || lowerName.contains("тотал по картам")
                || (hasMapWord && extractMapNumber(lowerName) == null);
    }

    private boolean isRoundHandicapMarket(VaidebetStakeGroupData group, String lowerName) {
        boolean hasHandicap = lowerName.contains("handicap") || lowerName.contains("фора");
        boolean hasRound = lowerName.contains("round") || lowerName.contains("раунд");
        return hasHandicap && hasRound;
    }

    private boolean isRoundTotalMarket(VaidebetStakeGroupData group, String lowerName) {
        boolean hasTotal = lowerName.contains("total") || lowerName.contains("тотал") || lowerName.contains("over/under") || lowerName.contains("o/u");
        boolean hasRound = lowerName.contains("round") || lowerName.contains("раунд");
        return hasTotal && hasRound;
    }

    private void handleMapWinner(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveMapScope(group);
        String groupName = getGroupName(group);
        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);

        for (VaidebetStakeData stake : group.getStakes()) {
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

    private void handleMapHandicap(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double param = resolveHandicapParam(stake, group);
            if (param == null) continue;

            HandicapBet.Outcome outcome = resolveHandicapOutcome(stake, match);
            if (outcome == null) continue;

            boolean isAsian = isQuarterAsian(param);
            HandicapBet betType = new HandicapBet(BetScope.FULL_MATCH, outcome, param, isAsian, StatType.MAPS);
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    private void handleMapTotal(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double param = resolveTotalParam(stake, group);
            if (param == null || param < 0) continue;

            TotalBet.Direction direction = resolveTotalDirection(stake);
            if (direction == null) continue;

            BetSubject subject = resolveTotalSubject(group, stake, match);
            boolean isAsian = isQuarterAsian(param);

            TotalBet betType = new TotalBet(BetScope.FULL_MATCH, subject, direction, param, isAsian, StatType.MAPS);
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    private void handleRoundHandicap(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveMapScope(group);
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
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

    private void handleRoundTotal(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveMapScope(group);
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
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

    private void handleGenericEsportsHandicap(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double param = resolveHandicapParam(stake, group);
            if (param == null) continue;

            HandicapBet.Outcome outcome = resolveHandicapOutcome(stake, match);
            if (outcome == null) continue;

            boolean isAsian = isQuarterAsian(param);
            HandicapBet betType = new HandicapBet(scope, outcome, param, isAsian, StatType.MATCH);
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    private void handleGenericEsportsTotal(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);

        for (VaidebetStakeData stake : group.getStakes()) {
            Double odd = stake.getFactor();
            if (odd == null || odd <= 1.0) continue;

            Double param = resolveTotalParam(stake, group);
            if (param == null || param < 0) continue;

            TotalBet.Direction direction = resolveTotalDirection(stake);
            if (direction == null) continue;

            BetSubject subject = resolveTotalSubject(group, stake, match);
            boolean isAsian = isQuarterAsian(param);

            TotalBet betType = new TotalBet(scope, subject, direction, param, isAsian, StatType.MATCH);
            addOddItem(items, stake, groupName, betType, param);
        }
    }

    private void handleGenericEsportsWinner(VaidebetStakeGroupData group, MatchCache match, SportType sportType, List<OddItem> items) {
        BetScope scope = resolveScope(group, sportType);
        String groupName = getGroupName(group);
        boolean hasDraw = group.getStakes().stream().anyMatch(this::isDrawStake);

        for (VaidebetStakeData stake : group.getStakes()) {
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

    private BetScope resolveMapScope(VaidebetStakeGroupData group) {
        Long id = group.getId();
        if (id != null) {
            if (id == 703L) return BetScope.MAP_1;
            if (id == 704L) return BetScope.MAP_2;
            if (id == 705L) return BetScope.MAP_3;
        }

        String name = getGroupName(group);
        Integer mapNum = extractMapNumber(name);
        if (mapNum != null) {
            return switch (mapNum) {
                case 1 -> BetScope.MAP_1;
                case 2 -> BetScope.MAP_2;
                case 3 -> BetScope.MAP_3;
                case 4 -> BetScope.MAP_4;
                case 5 -> BetScope.MAP_5;
                default -> BetScope.FULL_MATCH;
            };
        }
        return BetScope.FULL_MATCH;
    }

    private Integer extractMapNumber(String text) {
        if (text == null || text.isBlank()) return null;

        Matcher m = MAP_AFTER_KEYWORD.matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) {}
        }

        m = MAP_BEFORE_KEYWORD.matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) {}
        }

        m = MAP_ORDINAL_WORD.matcher(text);
        if (m.find()) {
            String word = m.group(1).toLowerCase();
            return switch (word) {
                case "first", "primeiro" -> 1;
                case "second", "segundo" -> 2;
                case "third", "terceiro" -> 3;
                case "fourth" -> 4;
                case "fifth" -> 5;
                case "sixth" -> 6;
                case "seventh" -> 7;
                default -> null;
            };
        }

        return null;
    }

    private boolean hasMapKeyword(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("map ") || lower.contains("map#") || lower.contains("карта ")
                || lower.contains("карта#") || lower.contains("mapa ")
                || MAP_AFTER_KEYWORD.matcher(lower).find()
                || MAP_BEFORE_KEYWORD.matcher(lower).find()
                || MAP_ORDINAL_WORD.matcher(lower).find();
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

    private boolean isDrawStake(VaidebetStakeData s) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim().toUpperCase() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim().toUpperCase() : "";
        return "X".equals(en) || "X".equals(ru) || "Х".equals(en) || "Х".equals(ru)
                || "DRAW".equals(en) || "НИЧЬЯ".equals(ru)
                || en.contains("DRAW") || ru.contains("НИЧЬЯ");
    }

    private boolean isTeam1ResultStake(VaidebetStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("1".equalsIgnoreCase(en) || "Win1".equalsIgnoreCase(en) || "W1".equalsIgnoreCase(en)
                || "П1".equalsIgnoreCase(ru) || "P1".equalsIgnoreCase(en) || "Home".equalsIgnoreCase(en)
                || "Победа 1".equalsIgnoreCase(ru) || "Team 1".equalsIgnoreCase(en)) {
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

    private boolean isTeam2ResultStake(VaidebetStakeData s, MatchCache match) {
        if (s == null) return false;
        String en = s.getNameEn() != null ? s.getNameEn().trim() : "";
        String ru = s.getNameRu() != null ? s.getNameRu().trim() : "";
        if ("2".equalsIgnoreCase(en) || "Win2".equalsIgnoreCase(en) || "W2".equalsIgnoreCase(en)
                || "П2".equalsIgnoreCase(ru) || "P2".equalsIgnoreCase(en) || "Away".equalsIgnoreCase(en)
                || "Победа 2".equalsIgnoreCase(ru) || "Team 2".equalsIgnoreCase(en)) {
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

    private HandicapBet.Outcome resolveHandicapOutcome(VaidebetStakeData stake, MatchCache match) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim() : "";
        String upperEn = en.toUpperCase();
        String upperRu = ru.toUpperCase();

        if (upperEn.equals("1") || upperEn.equals("H1") || upperRu.equals("Ф1")
                || upperEn.equals("HANDICAP 1") || upperEn.equals("HOME")
                || upperEn.startsWith("1 ") || upperRu.startsWith("1 ")
                || upperEn.startsWith("H1 ") || upperRu.startsWith("Ф1 ") || upperRu.startsWith("Ф1(")) {
            return HandicapBet.Outcome.TEAM1;
        }

        if (upperEn.equals("2") || upperEn.equals("H2") || upperRu.equals("Ф2")
                || upperEn.equals("HANDICAP 2") || upperEn.equals("AWAY")
                || upperEn.startsWith("2 ") || upperRu.startsWith("2 ")
                || upperEn.startsWith("H2 ") || upperRu.startsWith("Ф2 ") || upperRu.startsWith("Ф2(")) {
            return HandicapBet.Outcome.TEAM2;
        }

        if (match != null) {
            if (match.getTeam1() != null && !match.getTeam1().isBlank()) {
                String t1 = match.getTeam1().trim();
                if (en.equalsIgnoreCase(t1) || ru.equalsIgnoreCase(t1)) {
                    return HandicapBet.Outcome.TEAM1;
                }
            }
            if (match.getTeam2() != null && !match.getTeam2().isBlank()) {
                String t2 = match.getTeam2().trim();
                if (en.equalsIgnoreCase(t2) || ru.equalsIgnoreCase(t2)) {
                    return HandicapBet.Outcome.TEAM2;
                }
            }
        }

        return null;
    }

    private TotalBet.Direction resolveTotalDirection(VaidebetStakeData stake) {
        if (stake == null) return null;
        String en = stake.getNameEn() != null ? stake.getNameEn().trim().toUpperCase() : "";
        String ru = stake.getNameRu() != null ? stake.getNameRu().trim().toUpperCase() : "";

        if (en.equals("OVER") || ru.equals("БОЛЬШЕ") || ru.equals("Б") || en.equals("O")
                || en.startsWith("OVER") || ru.startsWith("БОЛЬШЕ") || ru.startsWith("ТБ")) {
            return TotalBet.Direction.OVER;
        }

        if (en.equals("UNDER") || ru.equals("МЕНЬШЕ") || ru.equals("М") || en.equals("U")
                || en.startsWith("UNDER") || ru.startsWith("МЕНЬШЕ") || ru.startsWith("ТМ")) {
            return TotalBet.Direction.UNDER;
        }

        return null;
    }

    private BetSubject resolveTotalSubject(VaidebetStakeGroupData group, VaidebetStakeData stake, MatchCache match) {
        String stakeEn = stake.getNameEn() != null ? stake.getNameEn() : "";
        String stakeRu = stake.getNameRu() != null ? stake.getNameRu() : "";
        String stakeCombined = stakeEn + " " + stakeRu;

        boolean stakeT1 = TEAM1_PATTERN.matcher(stakeCombined).find();
        boolean stakeT2 = TEAM2_PATTERN.matcher(stakeCombined).find();

        if (stakeT1 && !stakeT2) return BetSubject.TEAM1;
        if (stakeT2 && !stakeT1) return BetSubject.TEAM2;

        return BetSubject.MATCH;
    }

    private Double resolveTotalParam(VaidebetStakeData stake, VaidebetStakeGroupData group) {
        if (stake.getArgument() != null) return stake.getArgument();
        Double parsed = extractNumericParam(stake.getNameEn());
        if (parsed == null) parsed = extractNumericParam(stake.getNameRu());
        if (parsed == null) parsed = extractNumericParam(getGroupName(group));
        return parsed;
    }

    private Double resolveHandicapParam(VaidebetStakeData stake, VaidebetStakeGroupData group) {
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
