package pro.datawiki.igaming.source.sport888.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiOutcome {
    private Long id;
    private String label;
    private Integer odds;
    private String type;
    private Double line;
    private String participant;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getOdds() { return odds; }
    public void setOdds(Integer odds) { this.odds = odds; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getLine() { return line; }
    public void setLine(Double line) { this.line = line; }
    public String getParticipant() { return participant; }
    public void setParticipant(String participant) { this.participant = participant; }
}
