package pro.datawiki.igaming.source.fanduel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "fanduel")
@Getter
@Setter
public class FanDuelConfig {
    
    private Api api = new Api();
    private Fetch fetch = new Fetch();

    @Getter
    @Setter
    public static class Api {
        private String baseUrl = "https://sbapi.ny.sportsbook.fanduel.com";
        private String apiKey = "FhMFpcPWXMeyZxOx";
        private String siteId = "US-SB";
    }

    @Getter
    @Setter
    public static class Fetch {
        private long delayMs = 10000;
        private List<Long> sportEventTypeIds = List.of(
            6423L,    // American Football (NFL / CFB)
            1L,       // Soccer
            7522L,    // Basketball (NBA)
            7524L,    // Ice Hockey (NHL)
            7511L,    // Baseball (MLB)
            2L,       // Tennis
            26420387L,// MMA
            6L,       // Boxing
            3L,       // Golf
            8L        // Motor Sport
        );
        private List<String> customPages = List.of(
            "nfl",
            "nba",
            "nhl",
            "mlb"
        );
    }
}

