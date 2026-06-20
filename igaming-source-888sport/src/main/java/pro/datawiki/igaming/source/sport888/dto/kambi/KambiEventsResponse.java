package pro.datawiki.igaming.source.sport888.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiEventsResponse {
    private List<KambiEventWrapper> events;

    public List<KambiEventWrapper> getEvents() { return events; }
    public void setEvents(List<KambiEventWrapper> events) { this.events = events; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KambiEventWrapper {
        private KambiEvent event;
        private List<KambiBetOffer> betOffers;

        public KambiEvent getEvent() { return event; }
        public void setEvent(KambiEvent event) { this.event = event; }
        public List<KambiBetOffer> getBetOffers() { return betOffers; }
        public void setBetOffers(List<KambiBetOffer> betOffers) { this.betOffers = betOffers; }
    }
}
