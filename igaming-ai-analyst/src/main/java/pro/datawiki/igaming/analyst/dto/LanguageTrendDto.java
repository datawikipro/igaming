package pro.datawiki.igaming.analyst.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Metric breakdown of Google Trends search interest for a specific language persona.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguageTrendDto implements Serializable {

    @JsonProperty("score")
    private Integer score;

    @JsonProperty("trend_direction")
    @JsonAlias({"trendDirection", "trend_direction"})
    private String trendDirection;

    @JsonProperty("rising_queries")
    @JsonAlias({"risingQueries", "rising_queries"})
    private List<String> risingQueries;

    @JsonProperty("top_queries")
    @JsonAlias({"topQueries", "top_queries"})
    private List<String> topQueries;
}
