package pro.datawiki.igaming.source.paf.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.*;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiEvent;
import pro.datawiki.igaming.source.core.engine.kambi.dto.KambiOutcome;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;

import java.util.List;
import java.util.Locale;

public abstract class AbstractPafMarketHandler extends AbstractBetTypeMapper implements PafMarketHandler {

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "paf".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    protected double extractDecimalOdds(KambiOutcome outcome) {
        if (outcome == null || outcome.getOdds() == null) {
            return 0.0;
        }
        return outcome.getOdds() / 1000.0;
    }

    protected BetScope resolveScope(String marketName) {
        if (marketName == null) {
            return BetScope.FULL_MATCH;
        }
        String m = marketName.toUpperCase(Locale.ROOT);
        // Half 1 (English, Swedish, Finnish)
        if (m.contains("1ST HALF") || m.contains("FIRST HALF") || m.contains("HALF TIME")
                || m.contains("1ST_HALF") || m.contains("FIRST_HALF") || m.contains("HALF 1")
                || m.contains("HT1") || m.contains("1H")
                || m.contains("1:A HALVLEK") || m.contains("1.A HALVLEK") || m.contains("FÖRSTA HALVLEK")
                || m.contains("HALVTID")
                || m.contains("1. PUOLIAIKA") || m.contains("1 PUOLIAIKA") || m.contains("ENSIMMÄINEN PUOLIAIKA")
                || m.contains("TAUOTULOS")) {
            return BetScope.HALF_1;
        }
        // Half 2 (English, Swedish, Finnish)
        if (m.contains("2ND HALF") || m.contains("SECOND HALF")
                || m.contains("2ND_HALF") || m.contains("SECOND_HALF") || m.contains("HALF 2")
                || m.contains("HT2") || m.contains("2H")
                || m.contains("2:A HALVLEK") || m.contains("2.A HALVLEK") || m.contains("ANDRA HALVLEK")
                || m.contains("2. PUOLIAIKA") || m.contains("2 PUOLIAIKA") || m.contains("TOINEN PUOLIAIKA")) {
            return BetScope.HALF_2;
        }
        // Periods (English, Swedish, Finnish)
        if (m.contains("1ST PERIOD") || m.contains("FIRST PERIOD") || m.contains("PERIOD 1") || m.contains("1P")
                || m.contains("1:A PERIOD") || m.contains("1.A PERIOD") || m.contains("FÖRSTA PERIODEN")
                || m.contains("1. ERÄ") || m.contains("1 ERÄ") || m.contains("ERÄ 1")) {
            return BetScope.PERIOD_1;
        }
        if (m.contains("2ND PERIOD") || m.contains("SECOND PERIOD") || m.contains("PERIOD 2") || m.contains("2P")
                || m.contains("2:A PERIOD") || m.contains("2.A PERIOD") || m.contains("ANDRA PERIODEN")
                || m.contains("2. ERÄ") || m.contains("2 ERÄ") || m.contains("ERÄ 2")) {
            return BetScope.PERIOD_2;
        }
        if (m.contains("3RD PERIOD") || m.contains("THIRD PERIOD") || m.contains("PERIOD 3") || m.contains("3P")
                || m.contains("3:E PERIOD") || m.contains("3.E PERIOD") || m.contains("TREDJE PERIODEN")
                || m.contains("3. ERÄ") || m.contains("3 ERÄ") || m.contains("ERÄ 3")) {
            return BetScope.PERIOD_3;
        }
        // Quarters (English, Swedish, Finnish)
        if (m.contains("1ST QUARTER") || m.contains("FIRST QUARTER") || m.contains("QUARTER 1") || m.contains("1Q")
                || m.contains("1:A KVART") || m.contains("1.A KVART") || m.contains("FÖRSTA KVARTEN")
                || m.contains("1. NELJÄNNES") || m.contains("NELJÄNNES 1")) {
            return BetScope.QUARTER_1;
        }
        if (m.contains("2ND QUARTER") || m.contains("SECOND QUARTER") || m.contains("QUARTER 2") || m.contains("2Q")
                || m.contains("2:A KVART") || m.contains("2.A KVART") || m.contains("ANDRA KVARTEN")
                || m.contains("2. NELJÄNNES") || m.contains("NELJÄNNES 2")) {
            return BetScope.QUARTER_2;
        }
        if (m.contains("3RD QUARTER") || m.contains("THIRD QUARTER") || m.contains("QUARTER 3") || m.contains("3Q")
                || m.contains("3:E KVART") || m.contains("3.E KVART") || m.contains("TREDJE KVARTEN")
                || m.contains("3. NELJÄNNES") || m.contains("NELJÄNNES 3")) {
            return BetScope.QUARTER_3;
        }
        if (m.contains("4TH QUARTER") || m.contains("FOURTH QUARTER") || m.contains("QUARTER 4") || m.contains("4Q")
                || m.contains("4:E KVART") || m.contains("4.E KVART") || m.contains("FJÄRDE KVARTEN")
                || m.contains("4. NELJÄNNES") || m.contains("NELJÄNNES 4")) {
            return BetScope.QUARTER_4;
        }
        // Sets (English, Swedish, Finnish)
        if (m.contains("1ST SET") || m.contains("SET 1") || m.contains("1:A SET") || m.contains("1. SETTI") || m.contains("SETTI 1")) {
            return BetScope.SET_1;
        }
        if (m.contains("2ND SET") || m.contains("SET 2") || m.contains("2:A SET") || m.contains("2. SETTI") || m.contains("SETTI 2")) {
            return BetScope.SET_2;
        }
        if (m.contains("3RD SET") || m.contains("SET 3") || m.contains("3:E SET") || m.contains("3. SETTI") || m.contains("SETTI 3")) {
            return BetScope.SET_3;
        }
        if (m.contains("4TH SET") || m.contains("SET 4") || m.contains("4:E SET") || m.contains("4. SETTI") || m.contains("SETTI 4")) {
            return BetScope.SET_4;
        }
        if (m.contains("5TH SET") || m.contains("SET 5") || m.contains("5:E SET") || m.contains("5. SETTI") || m.contains("SETTI 5")) {
            return BetScope.SET_5;
        }
        return BetScope.FULL_MATCH;
    }

    protected void addOddItem(List<OddItem> items, KambiOutcome outcome, String groupName,
                              String name, double odds, BetType betType) {
        if (odds <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return;
        }
        OddItem item = new OddItem();
        item.setFactorId(outcome != null && outcome.getId() != null ? String.valueOf(outcome.getId()) : (groupName + "_" + name.replace(" ", "_")));
        item.setGroupName(groupName);
        item.setName(name);
        item.setValue(odds);
        item.setBetType(betType);
        items.add(item);
    }

    protected void addMatchResult(List<OddItem> items, KambiOutcome outcome, String groupName, String name,
                                  double odds, BetScope scope, MatchResultBet.Outcome resultOutcome, StatType statType) {
        StatType st = statType != null ? statType : StatType.MATCH;
        addOddItem(items, outcome, groupName, name, odds, new MatchResultBet(scope, resultOutcome, st));
    }

    protected void addTotal(List<OddItem> items, KambiOutcome outcome, String groupName, String name,
                            double odds, BetScope scope, BetSubject subject, TotalBet.Direction direction,
                            double limit, boolean isAsian, StatType statType) {
        StatType st = statType != null ? statType : StatType.MATCH;
        BetSubject subj = subject != null ? subject : BetSubject.MATCH;
        addOddItem(items, outcome, groupName, name, odds, new TotalBet(scope, subj, direction, limit, isAsian, st));
    }

    protected void addHandicap(List<OddItem> items, KambiOutcome outcome, String groupName, String name,
                              double odds, BetScope scope, HandicapBet.Outcome handicapOutcome, double hdp,
                              boolean isAsian, StatType statType) {
        StatType st = statType != null ? statType : StatType.MATCH;
        addOddItem(items, outcome, groupName, name, odds, new HandicapBet(scope, handicapOutcome, hdp, isAsian, st));
    }

    protected void addBinary(List<OddItem> items, KambiOutcome outcome, String groupName, String name,
                             double odds, BetScope scope, BetSubject subject, BinaryMarketBet.MarketType marketType,
                             BinaryMarketBet.Outcome binaryOutcome, StatType statType) {
        StatType st = statType != null ? statType : StatType.MATCH;
        BetSubject subj = subject != null ? subject : BetSubject.MATCH;
        addOddItem(items, outcome, groupName, name, odds, new BinaryMarketBet(scope, subj, marketType, binaryOutcome, st));
    }
}
