package pro.datawiki.igaming.source.sport888.dto.spectate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpectateMarket {
    private Long id;
    private String name;
    private Map<String, SpectateSelection> selections;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, SpectateSelection> getSelections() { return selections; }
    public void setSelections(Map<String, SpectateSelection> selections) { this.selections = selections; }
}
