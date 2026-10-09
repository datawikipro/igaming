package pro.datawiki.igaming.source.parimatch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParimatchEventsResponse {
    @JsonProperty("events")
    private List<ParimatchEvent> events = new ArrayList<>();

    @JsonProperty("sports")
    private List<ParimatchSport> sports = new ArrayList<>();
}
