package pro.datawiki.igaming.source.bet365.service;

import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.bet365.config.Bet365Config;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class Bet365ApiClient {

    private final RestTemplate restTemplate;
    private final Bet365Config bet365Config;

    public Bet365EventsResponse getEvents() {
        // In a real loader, we would call bet365Config.getApi().getBaseUrl()
        // Here we build a mock response for skeleton integration testing
        Bet365EventsResponse response = new Bet365EventsResponse();
        List<Bet365Event> list = new ArrayList<>();
        
        // Add a mock football event
        Bet365Event event1 = new Bet365Event();
        event1.setId("b365_evt_1001");
        event1.setSport("Soccer");
        event1.setLeague("UEFA Champions League");
        event1.setHomeTeam("Real Madrid");
        event1.setAwayTeam("Bayern Munich");
        event1.setStartTime(System.currentTimeMillis() + 3600000); // starts in 1 hour
        event1.setLive(false);
        list.add(event1);

        // Add a mock tennis event
        Bet365Event event2 = new Bet365Event();
        event2.setId("b365_evt_1002");
        event2.setSport("Tennis");
        event2.setLeague("Wimbledon");
        event2.setHomeTeam("Novak Djokovic");
        event2.setAwayTeam("Carlos Alcaraz");
        event2.setStartTime(System.currentTimeMillis() - 600000); // started 10 minutes ago
        event2.setLive(true);
        list.add(event2);

        response.setEvents(list);
        return response;
    }

    public Bet365OddsResponse getEventOdds(String eventId) {
        Bet365OddsResponse response = new Bet365OddsResponse();
        response.setEventId(eventId);
        List<Bet365Odd> odds = new ArrayList<>();

        if ("b365_evt_1001".equals(eventId)) {
            odds.add(new Bet365Odd("1", "1X2", "Real Madrid", 1.85));
            odds.add(new Bet365Odd("X", "1X2", "Draw", 3.60));
            odds.add(new Bet365Odd("2", "1X2", "Bayern Munich", 4.10));
        } else if ("b365_evt_1002".equals(eventId)) {
            odds.add(new Bet365Odd("1", "Match Winner", "Novak Djokovic", 1.65));
            odds.add(new Bet365Odd("2", "Match Winner", "Carlos Alcaraz", 2.20));
        }
        response.setOdds(odds);
        return response;
    }

    @Getter
    @Setter
    public static class Bet365EventsResponse {
        private List<Bet365Event> events;
    }

    @Getter
    @Setter
    public static class Bet365Event {
        private String id;
        private String sport;
        private String league;
        private String homeTeam;
        private String awayTeam;
        private long startTime;
        private boolean isLive;
    }

    @Getter
    @Setter
    public static class Bet365OddsResponse {
        private String eventId;
        private List<Bet365Odd> odds;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Bet365Odd {
        private String code;
        private String marketName;
        private String outcomeName;
        private double value;
    }
}
