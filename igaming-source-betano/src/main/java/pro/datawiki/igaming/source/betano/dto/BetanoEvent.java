package pro.datawiki.igaming.source.betano.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetanoEvent {
    private String id;
    
    @JsonProperty("eventId")
    private String eventId;

    private String name;
    
    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("title")
    private String title;

    @JsonProperty("startTime")
    private Long startTime;

    @JsonProperty("startTimeEpoch")
    private Long startTimeEpoch;

    @JsonProperty("sportId")
    private String sportId;

    @JsonProperty("sportName")
    private String sportName;

    @JsonProperty("regionName")
    private String regionName;

    @JsonProperty("leagueName")
    private String leagueName;

    @JsonProperty("tournamentName")
    private String tournamentName;

    @JsonProperty("isLive")
    private Boolean isLive;

    @JsonProperty("live")
    private Boolean live;

    @JsonProperty("markets")
    private List<BetanoMarket> markets;

    @JsonProperty("participants")
    private List<java.util.Map<String, Object>> participants;

    @JsonProperty("competitors")
    private List<java.util.Map<String, Object>> competitors;

    public String getResolvedId() {
        String val = id != null ? id : eventId;
        if (val == null || val.isBlank() || !val.matches("^\\d+$")) {
            return null;
        }
        return val;
    }

    public String getResolvedName() {
        if (shortName != null && !shortName.isBlank() && (shortName.contains("-") || shortName.contains("vs") || shortName.contains("—"))) return shortName;
        if (name != null && !name.isBlank() && (name.contains("-") || name.contains("vs") || name.contains("—"))) return name;
        if (title != null && !title.isBlank() && (title.contains("-") || title.contains("vs") || title.contains("—"))) return title;
        if (participants != null && participants.size() >= 2) {
            String p1 = extractParticipantName(participants.get(0));
            String p2 = extractParticipantName(participants.get(1));
            if (p1 != null && p2 != null) return p1 + " - " + p2;
        }
        if (competitors != null && competitors.size() >= 2) {
            String c1 = extractParticipantName(competitors.get(0));
            String c2 = extractParticipantName(competitors.get(1));
            if (c1 != null && c2 != null) return c1 + " - " + c2;
        }
        if (shortName != null && !shortName.isBlank()) return shortName;
        if (name != null && !name.isBlank()) return name;
        if (title != null && !title.isBlank()) return title;
        return "";
    }

    public String[] getResolvedTeams() {
        if (participants != null && participants.size() >= 2) {
            String p1 = extractParticipantName(participants.get(0));
            String p2 = extractParticipantName(participants.get(1));
            if (p1 != null && p2 != null && !p1.isBlank() && !p2.isBlank()) {
                return new String[]{p1.trim(), p2.trim()};
            }
        }
        if (competitors != null && competitors.size() >= 2) {
            String c1 = extractParticipantName(competitors.get(0));
            String c2 = extractParticipantName(competitors.get(1));
            if (c1 != null && c2 != null && !c1.isBlank() && !c2.isBlank()) {
                return new String[]{c1.trim(), c2.trim()};
            }
        }
        String resolved = getResolvedName();
        if (resolved != null && !resolved.isBlank()) {
            String[] parts = resolved.split("(?i)\\s+(?:vs\\.?|v\\.?|—|-)\\s+");
            if (parts.length >= 2 && !parts[0].isBlank() && !parts[1].isBlank()) {
                return new String[]{parts[0].trim(), parts[1].trim()};
            }
        }
        return null;
    }

    private String extractParticipantName(java.util.Map<String, Object> map) {
        if (map == null) return null;
        Object n = map.get("name");
        if (n == null) n = map.get("shortName");
        if (n == null) n = map.get("text");
        if (n == null) n = map.get("title");
        return n != null ? n.toString() : null;
    }

    public String getResolvedLeague() {
        if (leagueName != null && !leagueName.isBlank()) return leagueName;
        if (tournamentName != null && !tournamentName.isBlank()) return tournamentName;
        if (regionName != null && !regionName.isBlank()) return regionName;
        return "General";
    }

    public String getResolvedSport() {
        if (sportName != null && !sportName.isBlank()) return normalizeSport(sportName);
        if (sportId != null && !sportId.isBlank()) return normalizeSport(sportId);
        return "FOOTBALL";
    }

    public static String normalizeSport(String raw) {
        if (raw == null || raw.isBlank()) return "FOOTBALL";
        String s = raw.trim().toUpperCase(java.util.Locale.ROOT);
        return switch (s) {
            case "1", "FOOT", "FUSSBALL", "FOOTBALL", "SOCCER" -> "FOOTBALL";
            case "2", "BASK", "BASKETBALL" -> "BASKETBALL";
            case "3", "TENN", "TENNIS" -> "TENNIS";
            case "4", "ICEH", "EISHOCKEY", "ICE_HOCKEY", "HOCKEY" -> "ICE_HOCKEY";
            case "5", "HAND", "HANDBALL" -> "HANDBALL";
            case "6", "VOLL", "VOLLEYBALL" -> "VOLLEYBALL";
            case "7", "BASE", "BASEBALL" -> "BASEBALL";
            case "DART", "DARTS" -> "DARTS";
            case "MMA", "COMBAT", "BOXING" -> "MMA";
            case "TISCH", "TISCHTENNIS", "TABLE_TENNIS" -> "TABLE_TENNIS";
            case "ESPORT", "ESPORTS" -> "ESPORTS";
            case "FUTSAL" -> "FUTSAL";
            case "AMERICAN_FOOTBALL", "AMERICAN-FOOTBALL" -> "AMERICAN_FOOTBALL";
            case "BADMINTON" -> "BADMINTON";
            case "SNOOKER" -> "SNOOKER";
            case "RUGBY", "RUGBY_UNION", "RUGBY-UNION" -> "RUGBY";
            default -> s;
        };
    }

    public Boolean getResolvedIsLive() {
        if (isLive != null) return isLive;
        if (live != null) return live;
        return Boolean.FALSE;
    }

    public Long getResolvedStartTime() {
        Long val = null;
        if (startTime != null && startTime > 0) val = startTime;
        else if (startTimeEpoch != null && startTimeEpoch > 0) val = startTimeEpoch;
        if (val == null || val <= 0) {
            val = System.currentTimeMillis() + (Boolean.TRUE.equals(getResolvedIsLive()) ? 0 : 3600_000L);
        }
        return val;
    }
}
