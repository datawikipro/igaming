package pro.datawiki.igaming.playerfaces.domain;

/**
 * Professional tour or promotion designation for solo sports athletes.
 */
public enum TourType {
    ATP("ATP Tour"),
    WTA("WTA Tour"),
    ITF("ITF Circuit"),
    CHALLENGER("ATP Challenger Tour"),
    UFC("Ultimate Fighting Championship"),
    BELLATOR("Bellator MMA"),
    ONE_FC("ONE Championship"),
    PFL("Professional Fighters League"),
    OTHER("Other Tour/Promotion");

    private final String displayName;

    TourType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TourType fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return OTHER;
        }
        String clean = raw.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (TourType tour : values()) {
            if (tour.name().equalsIgnoreCase(clean) || tour.displayName.equalsIgnoreCase(raw.trim())) {
                return tour;
            }
        }
        return OTHER;
    }
}
