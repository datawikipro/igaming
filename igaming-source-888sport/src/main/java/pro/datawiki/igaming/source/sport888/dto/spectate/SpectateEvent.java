package pro.datawiki.igaming.source.sport888.dto.spectate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpectateEvent {
    private Long id;
    private String start_time;
    private String name;
    private String tournament_display_name;
    private String sport_name;
    private Map<String, SpectateMarket> markets;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStart_time() { return start_time; }
    public void setStart_time(String start_time) { this.start_time = start_time; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTournament_display_name() { return tournament_display_name; }
    public void setTournament_display_name(String tournament_display_name) { this.tournament_display_name = tournament_display_name; }
    public String getSport_name() { return sport_name; }
    public void setSport_name(String sport_name) { this.sport_name = sport_name; }
    public Map<String, SpectateMarket> getMarkets() { return markets; }
    public void setMarkets(Map<String, SpectateMarket> markets) { this.markets = markets; }
}
