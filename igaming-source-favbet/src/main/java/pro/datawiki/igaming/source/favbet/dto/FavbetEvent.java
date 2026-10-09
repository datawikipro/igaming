package pro.datawiki.igaming.source.favbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FavbetEvent {
    @JsonProperty("event_id")
    private Long eventId;

    @JsonProperty("event_name")
    private String eventName;

    @JsonProperty("event_dt")
    private Long eventDt;

    @JsonProperty("sport_id")
    private Long sportId;

    @JsonProperty("sport_name")
    private String sportName;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("tournament_id")
    private Long tournamentId;

    @JsonProperty("tournament_name")
    private String tournamentName;

    @JsonProperty("is_live")
    private Boolean isLive;

    @JsonProperty("head_markets")
    private List<FavbetMarket> headMarkets = new ArrayList<>();

    @JsonProperty("markets")
    private List<FavbetMarket> markets = new ArrayList<>();

    public List<FavbetMarket> getAllMarkets() {
        List<FavbetMarket> all = new ArrayList<>();
        if (markets != null && !markets.isEmpty()) {
            all.addAll(markets);
        }
        if (headMarkets != null && !headMarkets.isEmpty()) {
            all.addAll(headMarkets);
        }
        return all;
    }
}
