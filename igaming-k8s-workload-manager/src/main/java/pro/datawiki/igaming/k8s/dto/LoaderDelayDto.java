package pro.datawiki.igaming.k8s.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoaderDelayDto {

    @JsonProperty("bookmaker")
    private String bookmaker;

    @JsonProperty("oddsCount")
    private Long oddsCount;

    @JsonProperty("matchesCount")
    private Long matchesCount;

    @JsonProperty("delayMinutes")
    private Double delayMinutes;
}
