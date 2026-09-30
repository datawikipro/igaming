package pro.datawiki.igaming.source.digitain.service.handler;

import pro.datawiki.igaming.dto.BetType;
import pro.datawiki.igaming.dto.OddItem;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.source.core.mapper.AbstractBetTypeMapper;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeData;
import pro.datawiki.igaming.source.digitain.dto.DigitainStakeGroupData;

public abstract class AbstractDigitainMarketHandler extends AbstractBetTypeMapper implements DigitainMarketHandler {

    @Override
    public boolean supports(String bookmaker, SportType sportType) {
        return "digitain".equalsIgnoreCase(bookmaker);
    }

    @Override
    public BetType map(String m, String o, Double param) {
        return null;
    }

    protected OddItem createOddItem(DigitainStakeData stake, String marketName, BetType betType, Double param) {
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

    protected String getGroupName(DigitainStakeGroupData group) {
        if (group == null) return "";
        if (group.getNameEn() != null && !group.getNameEn().isBlank()) {
            return group.getNameEn();
        }
        return group.getNameRu() != null ? group.getNameRu() : "";
    }
}
