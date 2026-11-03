package pro.datawiki.igaming.playerfaces.domain;

/**
 * Tennis court surface classifications.
 */
public enum TennisSurface {
    HARD("Hard"),
    CLAY("Clay"),
    GRASS("Grass"),
    INDOOR("Indoor");

    private final String displayName;

    TennisSurface(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TennisSurface fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return HARD;
        }
        String clean = raw.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (clean.contains("CLAY")) {
            return CLAY;
        }
        if (clean.contains("GRASS")) {
            return GRASS;
        }
        if (clean.contains("INDOOR") || clean.contains("CARPET")) {
            return INDOOR;
        }
        return HARD;
    }
}
