package pro.datawiki.igaming.source.betfair.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "betfair")
@Getter
@Setter
public class BetfairConfig {
    
    private Api api = new Api();
    private Fetch fetch = new Fetch();

    @Getter
    @Setter
    public static class Api {
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class Fetch {
        private long delayMs = 15000;
    }
}
