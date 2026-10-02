package pro.datawiki.igaming.playerfaces.domain;

/**
 * Supported solo sport categories for athlete headshot harvesting and presentation.
 */
public enum SportType {
    TENNIS("Tennis"),
    MMA("Mixed Martial Arts"),
    BOXING("Boxing"),
    TABLE_TENNIS("Table Tennis"),
    BADMINTON("Badminton"),
    OTHER("Other");

    private final String displayName;

    SportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SportType fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return OTHER;
        }
        String clean = raw.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (SportType type : values()) {
            if (type.name().equalsIgnoreCase(clean) || type.displayName.equalsIgnoreCase(raw.trim())) {
                return type;
            }
        }
        return OTHER;
    }
}
