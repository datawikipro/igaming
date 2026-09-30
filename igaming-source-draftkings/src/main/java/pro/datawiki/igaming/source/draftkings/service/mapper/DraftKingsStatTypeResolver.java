package pro.datawiki.igaming.source.draftkings.service.mapper;

import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.SportType;
import pro.datawiki.igaming.dto.market.StatType;

@Component
public class DraftKingsStatTypeResolver {

    public StatType resolve(String categoryName, String marketName, SportType sportType) {
        String combined = ((categoryName != null ? categoryName : "") + " " + (marketName != null ? marketName : "")).toLowerCase();

        // 1. Corners
        if (combined.contains("corner")) {
            return StatType.CORNERS;
        }

        // 2. Yellow Cards & Cards
        if (combined.contains("yellow card")) {
            return StatType.YELLOW_CARDS;
        }
        if (combined.contains("booking") || combined.contains("red card") || combined.contains("card")) {
            return StatType.CARDS;
        }

        // 3. Offsides & Fouls
        if (combined.contains("offside")) {
            return StatType.OFFSIDES;
        }
        if (combined.contains("foul")) {
            return StatType.FOULS;
        }

        // 4. Shots
        if (combined.contains("shots on target") || combined.contains("shot on target") || combined.contains("sot")) {
            return StatType.SHOTS_ON_TARGET;
        }
        if (combined.contains("shots on goal") || combined.contains("shot on goal") || combined.contains("sog")) {
            return StatType.SHOTS_ON_GOAL;
        }

        // 5. Esports specific stats
        if (isEsports(sportType) || combined.contains("esports") || combined.contains("cs2") || combined.contains("dota") || combined.contains("lol")) {
            if (combined.contains("first blood")) {
                return StatType.FIRST_BLOOD;
            }
            if (combined.contains("tower")) {
                return StatType.TOWERS;
            }
            if (combined.contains("roshan")) {
                return StatType.ROSHAN;
            }
            if (combined.contains("baron")) {
                return StatType.BARON;
            }
            if (combined.contains("kill")) {
                return StatType.KILLS;
            }
            if (combined.contains("round")) {
                return StatType.ROUNDS;
            }
            if (combined.contains("total maps") || combined.contains("map handicap")) {
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
