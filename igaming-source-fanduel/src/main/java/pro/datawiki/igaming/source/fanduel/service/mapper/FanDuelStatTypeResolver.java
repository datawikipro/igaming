package pro.datawiki.igaming.source.fanduel.service.mapper;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.StatType;

@Component
public class FanDuelStatTypeResolver {

    public StatType resolve(String marketName, SportType sportType) {
        if (marketName == null || marketName.isBlank()) {
            return StatType.MATCH;
        }
        String text = marketName.toLowerCase();

        // 1. Yellow cards before generic cards
        if (text.contains("yellow card")) {
            return StatType.YELLOW_CARDS;
        }
        if (text.contains("booking") || text.contains("red card") || text.contains("card")) {
            return StatType.CARDS;
        }

        // 2. Corners
        if (text.contains("corner")) {
            return StatType.CORNERS;
        }

        // 3. Offsides & Fouls
        if (text.contains("offside")) {
            return StatType.OFFSIDES;
        }
        if (text.contains("foul")) {
            return StatType.FOULS;
        }

        // 4. Shots
        if (text.contains("shots on target") || text.contains("shot on target") || text.contains("sot")) {
            return StatType.SHOTS_ON_TARGET;
        }
        if (text.contains("shots on goal") || text.contains("shot on goal") || text.contains("sog")) {
            return StatType.SHOTS_ON_GOAL;
        }

        // 5. Esports specific stats
        if (isEsports(sportType) || text.contains("esports") || text.contains("cs2") || text.contains("dota") || text.contains("lol")) {
            if (text.contains("first blood")) {
                return StatType.FIRST_BLOOD;
            }
            if (text.contains("tower")) {
                return StatType.TOWERS;
            }
            if (text.contains("roshan")) {
                return StatType.ROSHAN;
            }
            if (text.contains("baron")) {
                return StatType.BARON;
            }
            if (text.contains("kill")) {
                return StatType.KILLS;
            }
            if (text.contains("round")) {
                return StatType.ROUNDS;
            }
            if (text.contains("total maps") || text.contains("map handicap")) {
                return StatType.MAPS;
            }
        }

        return StatType.MATCH;
    }

    private boolean isEsports(SportType st) {
        if (st == null) return false;
        return st == SportType.ESPORTS || st == SportType.CS2 || st == SportType.DOTA2
                || st == SportType.LEAGUE_OF_LEGENDS || st == SportType.VALORANT
                || st == SportType.RAINBOW_SIX || st == SportType.ROCKET_LEAGUE
                || st == SportType.CALL_OF_DUTY || st == SportType.OVERWATCH
                || st == SportType.PUBG || st == SportType.MOBILE_LEGENDS
                || st == SportType.STARCRAFT;
    }
}
