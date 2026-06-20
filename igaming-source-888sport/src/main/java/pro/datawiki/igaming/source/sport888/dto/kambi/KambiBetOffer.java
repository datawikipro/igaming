package pro.datawiki.igaming.source.sport888.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiBetOffer {
    private Long id;
    private KambiCriterion criterion;
    private List<KambiOutcome> outcomes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public KambiCriterion getCriterion() { return criterion; }
    public void setCriterion(KambiCriterion criterion) { this.criterion = criterion; }
    public List<KambiOutcome> getOutcomes() { return outcomes; }
    public void setOutcomes(List<KambiOutcome> outcomes) { this.outcomes = outcomes; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KambiCriterion {
        private Long id;
        private String label;
        private String englishLabel;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getEnglishLabel() { return englishLabel; }
        public void setEnglishLabel(String englishLabel) { this.englishLabel = englishLabel; }
    }
}
