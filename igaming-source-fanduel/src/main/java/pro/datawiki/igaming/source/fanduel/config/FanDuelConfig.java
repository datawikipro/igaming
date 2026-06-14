package pro.datawiki.igaming.source.fanduel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
        private String baseUrl = "https://sportsbook-us-east-1.fanduel.com";
        private String siteId = "US-SB";
    }

    @Getter
    @Setter
    public static class Fetch {
        private long delayMs = 15000;
        private java.util.List<Integer> eventGroupIds = java.util.List.of(
            88670846, // NBA
            88670847, // NFL
            88670848, // MLB
            88670849, // NHL
            88671587, // UFC/MMA
            88671238  // English Premier League
        );
    }
}
