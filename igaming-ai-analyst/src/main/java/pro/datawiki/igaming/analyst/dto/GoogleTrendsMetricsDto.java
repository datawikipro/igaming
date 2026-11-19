package pro.datawiki.igaming.analyst.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Aggregated Google Trends interest metrics, language-specific breakdown,
 * search queries, and LLM generation eligibility flags.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleTrendsMetricsDto implements Serializable {

    @JsonProperty("queried_at")
    @JsonAlias({"queriedAt", "queried_at"})
    private String queriedAt;

    @JsonProperty("global_interest_score")
    @JsonAlias({"globalInterestScore", "global_interest_score", "search_volume_index", "searchVolumeIndex"})
    private Integer globalInterestScore;

    @JsonProperty("search_volume_index")
    @JsonAlias({"searchVolumeIndex", "search_volume_index"})
    private Integer searchVolumeIndex;

    @JsonProperty("query")
    private String query;

    @JsonProperty("target_language")
    @JsonAlias({"targetLanguage", "target_language"})
    private String targetLanguage;

    @JsonProperty("rising_queries")
    @JsonAlias({"risingQueries", "rising_queries"})
    private List<String> risingQueries;

    @JsonProperty("top_queries")
    @JsonAlias({"topQueries", "top_queries"})
    private List<String> topQueries;

    @JsonProperty("by_language")
    @JsonAlias({"byLanguage", "by_language"})
    private Map<String, LanguageTrendDto> byLanguage;

    @JsonProperty("eligible_for_llm")
    @JsonAlias({"eligibleForLlm", "eligible_for_llm"})
    private Map<String, Boolean> eligibleForLlm;

    /**
     * Fallback to aggregate rising queries from byLanguage if top-level list is empty.
     */
    public List<String> getRisingQueries() {
        if (risingQueries != null && !risingQueries.isEmpty()) {
            return risingQueries;
        }
        if (byLanguage != null) {
            List<String> combined = new ArrayList<>();
            for (LanguageTrendDto lt : byLanguage.values()) {
                if (lt != null && lt.getRisingQueries() != null) {
                    combined.addAll(lt.getRisingQueries());
                }
            }
            if (!combined.isEmpty()) {
                return combined;
            }
        }
        return risingQueries;
    }
}
