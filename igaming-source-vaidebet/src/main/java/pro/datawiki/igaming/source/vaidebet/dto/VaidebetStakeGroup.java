package pro.datawiki.igaming.source.vaidebet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VaidebetStakeGroup {

    @JsonProperty("Id")
    private Long id;

    @JsonProperty("N")
    private String name;

    @JsonProperty("Stakes")
    private List<VaidebetStake> stakes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<VaidebetStake> getStakes() { return stakes; }
    public void setStakes(List<VaidebetStake> stakes) { this.stakes = stakes; }
}
