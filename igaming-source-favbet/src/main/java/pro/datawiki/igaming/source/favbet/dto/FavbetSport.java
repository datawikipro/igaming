package pro.datawiki.igaming.source.favbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FavbetSport {
    @JsonProperty("sport_id")
    private Long sportId;

    @JsonProperty("sport_name")
    private String sportName;

    @JsonProperty("sport_slug")
    private String sportSlug;

    @JsonProperty("event_count")
    private Integer eventCount;
}
