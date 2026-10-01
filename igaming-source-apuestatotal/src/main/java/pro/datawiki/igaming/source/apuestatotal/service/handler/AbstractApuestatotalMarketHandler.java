package pro.datawiki.igaming.source.apuestatotal.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.BetScope;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

import java.util.List;

public abstract class AbstractApuestatotalMarketHandler extends AbstractBetTypeMapper implements ApuestatotalMarketHandler {

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "apuestatotal".equalsIgnoreCase(bookmaker) || "digitain".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    protected OddItem createOddItem(ApuestatotalStakeData stake, String marketName, BetType betType, Double param) {
        if (stake == null || stake.getFactor() == null || stake.getFactor() <= 1.0 || betType == null || "UNKNOWN".equals(betType.code())) {
            return null;
        }
        OddItem item = new OddItem();
        item.setFactorId(stake.getId() != null ? String.valueOf(stake.getId()) : (marketName + "_" + (stake.getNameEn() != null ? stake.getNameEn() : stake.getNameRu())));
        item.setGroupName(marketName);
        item.setName(stake.getNameEn() != null && !stake.getNameEn().isBlank() ? stake.getNameEn() : stake.getNameRu());
        item.setValue(stake.getFactor());
        item.setBetType(betType);
        return item;
    }

    protected void addOddItem(List<OddItem> items, ApuestatotalStakeData stake, String marketName, BetType betType, Double param) {
        OddItem item = createOddItem(stake, marketName, betType, param);
        if (item != null && items != null) {
            items.add(item);
        }
    }

    protected BetScope resolveScope(ApuestatotalStakeGroupData group, SportType sportType) {
        if (group == null) {
            return BetScope.FULL_MATCH;
        }
        Long id = group.getId();
        if (id != null) {
            if (id == 4L || id == 5L || id == 6L || id == 10L || id == 993L) {
                return BetScope.HALF_1;
            }
            if (id == 7L || id == 8L || id == 9L) {
                return BetScope.HALF_2;
            }
        }
        String name = getGroupName(group).toLowerCase();
        if (name.contains("1st half") || name.contains("1-й тайм") || name.contains("1 тайм")
                || name.contains("half 1") || name.contains("first half")
                || name.contains("1er tiempo") || name.contains("primer tiempo") || name.contains("1º tiempo")
                || name.contains("1 tempo") || name.contains("1º tempo")) {
            return BetScope.HALF_1;
        }
        if (name.contains("2nd half") || name.contains("2-й тайм") || name.contains("2 тайм")
                || name.contains("half 2") || name.contains("second half")
                || name.contains("2do tiempo") || name.contains("segundo tiempo") || name.contains("2º tiempo")
                || name.contains("2 tempo") || name.contains("2º tempo")) {
            return BetScope.HALF_2;
        }
        if (name.contains("1st period") || name.contains("1-й период") || name.contains("1 период")
                || name.contains("1er periodo") || name.contains("primer periodo") || name.contains("1º período")) {
            return BetScope.PERIOD_1;
        }
        if (name.contains("2nd period") || name.contains("2-й период") || name.contains("2 период")
                || name.contains("2do periodo") || name.contains("segundo periodo") || name.contains("2º período")) {
            return BetScope.PERIOD_2;
        }
        if (name.contains("3rd period") || name.contains("3-й период") || name.contains("3 период")
                || name.contains("3er periodo") || name.contains("tercer periodo") || name.contains("3º período")) {
            return BetScope.PERIOD_3;
        }
        if (name.contains("1st set") || name.contains("1-й сет") || name.contains("1 сет")
                || name.contains("1er set") || name.contains("primer set") || name.contains("1º set")) {
            return BetScope.SET_1;
        }
        if (name.contains("2nd set") || name.contains("2-й сет") || name.contains("2 сет")
                || name.contains("2do set") || name.contains("segundo set") || name.contains("2º set")) {
            return BetScope.SET_2;
        }
        if (name.contains("3rd set") || name.contains("3-й сет") || name.contains("3 сет")
                || name.contains("3er set") || name.contains("tercer set") || name.contains("3º set")) {
            return BetScope.SET_3;
        }
        if (name.contains("1st quarter") || name.contains("1-я четверть") || name.contains("1 четверть")
                || name.contains("1er cuarto") || name.contains("primer cuarto") || name.contains("1º quarto")) {
            return BetScope.QUARTER_1;
        }
        if (name.contains("2nd quarter") || name.contains("2-я четверть") || name.contains("2 четверть")
                || name.contains("2do cuarto") || name.contains("segundo cuarto") || name.contains("2º quarto")) {
            return BetScope.QUARTER_2;
        }
        if (name.contains("3rd quarter") || name.contains("3-я четверть") || name.contains("3 четверть")
                || name.contains("3er cuarto") || name.contains("tercer cuarto") || name.contains("3º quarto")) {
            return BetScope.QUARTER_3;
        }
        if (name.contains("4th quarter") || name.contains("4-я четверть") || name.contains("4 четверть")
                || name.contains("4to cuarto") || name.contains("cuarto cuarto") || name.contains("4º quarto")) {
            return BetScope.QUARTER_4;
        }
        return BetScope.FULL_MATCH;
    }

    protected String getGroupName(ApuestatotalStakeGroupData group) {
        if (group == null) return "";
        if (group.getNameEn() != null && !group.getNameEn().isBlank()) {
            return group.getNameEn();
        }
        return group.getNameRu() != null ? group.getNameRu() : "";
    }
}
