package pro.datawiki.igaming.source.matchbook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchbookEventsResponse {
    private Integer total;
    private Integer offset;

    @JsonProperty("per-page")
    private Integer perPage;

    private List<MatchbookEvent> events = new ArrayList<>();
}
