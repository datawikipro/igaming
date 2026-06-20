package pro.datawiki.igaming.source.bwin.dto.entain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntainGame {
    private Long id;
    private EntainValueObject name;
    private List<EntainResult> results;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EntainValueObject getName() { return name; }
    public void setName(EntainValueObject name) { this.name = name; }
    public List<EntainResult> getResults() { return results; }
    public void setResults(List<EntainResult> results) { this.results = results; }
}
