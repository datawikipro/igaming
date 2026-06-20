package pro.datawiki.igaming.source.sport888.dto.spectate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpectateEventsResponse {
    private List<SpectateEvent> events;

    public List<SpectateEvent> getEvents() { return events; }
    public void setEvents(List<SpectateEvent> events) { this.events = events; }
}
