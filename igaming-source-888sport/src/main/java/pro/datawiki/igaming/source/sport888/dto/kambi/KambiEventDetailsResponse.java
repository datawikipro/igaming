package pro.datawiki.igaming.source.sport888.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiEventDetailsResponse {
    private List<KambiEvent> events;
    private List<KambiBetOffer> betoffers;

    public List<KambiEvent> getEvents() { return events; }
    public void setEvents(List<KambiEvent> events) { this.events = events; }
    public List<KambiBetOffer> getBetoffers() { return betoffers; }
    public void setBetoffers(List<KambiBetOffer> betoffers) { this.betoffers = betoffers; }
}
