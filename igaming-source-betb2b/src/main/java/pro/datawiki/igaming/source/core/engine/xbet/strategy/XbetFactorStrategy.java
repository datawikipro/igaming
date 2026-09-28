package pro.datawiki.igaming.source.core.engine.xbet.strategy;

import pro.datawiki.igaming.dto.BetType;

public interface XbetFactorStrategy {
    boolean supports(String factorCode);
    BetType map(String factorCode, Double param);
    String describe(String factorCode, Double param);
}
