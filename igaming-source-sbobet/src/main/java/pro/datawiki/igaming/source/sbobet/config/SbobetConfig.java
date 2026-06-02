package pro.datawiki.igaming.source.sbobet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sbobet")
@Getter
@Setter
public class SbobetConfig {

    private Api api = new Api();
    private Fetch fetch = new Fetch();

    @Getter
    @Setter
    public static class Api {
        private String baseUrl = "https://www.sbobet.com";
        private String oddsUrl = "https://www.sbobet.com/web-root/delivery/odds";
    }

    @Getter
    @Setter
    public static class Fetch {
        private long delayMs = 60000;
    }
}
