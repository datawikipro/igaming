package pro.datawiki.igaming.source.dafabet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "dafabet")
@Getter
@Setter
public class DafabetConfig {

    private Api api = new Api();
    private Fetch fetch = new Fetch();

    @Getter
    @Setter
    public static class Api {
        private String baseUrl = "https://www.dafabet.com";
        private String oddsPattern = "/sports-api/";
    }

    @Getter
    @Setter
    public static class Fetch {
        private long delayMs = 60000;
    }
}
