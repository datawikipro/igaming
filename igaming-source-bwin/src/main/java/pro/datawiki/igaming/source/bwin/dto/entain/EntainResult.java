package pro.datawiki.igaming.source.bwin.dto.entain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntainResult {
    private Long id;
    private Double odds;
    private EntainValueObject name;
    private EntainValueObject sourceName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getOdds() { return odds; }
    public void setOdds(Double odds) { this.odds = odds; }
    public EntainValueObject getName() { return name; }
    public void setName(EntainValueObject name) { this.name = name; }
    public EntainValueObject getSourceName() { return sourceName; }
    public void setSourceName(EntainValueObject sourceName) { this.sourceName = sourceName; }
}
