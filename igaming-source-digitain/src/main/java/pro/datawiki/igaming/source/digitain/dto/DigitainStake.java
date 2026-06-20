package pro.datawiki.igaming.source.digitain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitainStake {

    @JsonProperty("Id")
    private Long id;

    @JsonProperty("N")
    private String name;

    @JsonProperty("F")
    private Double factor;

    @JsonProperty("IsWinner")
    private Boolean isWinner;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getFactor() { return factor; }
    public void setFactor(Double factor) { this.factor = factor; }
    public Boolean getIsWinner() { return isWinner; }
    public void setIsWinner(Boolean isWinner) { this.isWinner = isWinner; }
}
