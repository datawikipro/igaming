package pro.datawiki.igaming.source.bet365.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bet365")
@Getter
@Setter
public class Bet365Config {
    
    private Api api = new Api();
    private Fetch fetch = new Fetch();

    @Getter
    @Setter
    public static class Api {
        private String baseUrl;
        private String token;
    }

    @Getter
    @Setter
    public static class Fetch {
        private long delayMs = 15000;
    }
}
