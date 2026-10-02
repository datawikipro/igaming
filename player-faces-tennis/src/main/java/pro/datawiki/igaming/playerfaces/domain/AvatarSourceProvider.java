package pro.datawiki.igaming.playerfaces.domain;

/**
 * Provider source identifier for collected player headshots.
 */
public enum AvatarSourceProvider {
    WIKIDATA("Wikidata SPARQL / P18 Image"),
    WIKIMEDIA("Wikimedia Commons API"),
    THE_SPORTS_DB("TheSportsDB Player API"),
    SOFASCORE("SofaScore Media API"),
    FALLBACK_CYBERPUNK("SmartBet Cyberpunk SVG Dynamic Fallback"),
    CUSTOM("Custom Administrator Upload");

    private final String description;

    AvatarSourceProvider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
