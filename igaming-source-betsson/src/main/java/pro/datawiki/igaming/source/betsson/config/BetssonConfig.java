package pro.datawiki.igaming.source.betsson.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "betsson")
@Getter
@Setter
public class BetssonConfig {
    
    private Api api = new Api();
    private Fetch fetch = new Fetch();

    @Getter
    @Setter
    public static class Api {
        private String baseUrl;
        private String brand = "ub";
        private String locale = "en_GB";
        private String market = "GB";
    }

    @Getter
    @Setter
    public static class Fetch {
        private long delayMs = 15000;
    }
}
