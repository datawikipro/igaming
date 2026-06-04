package pro.datawiki.igaming.source.pinnacle.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pinnacle")
@Getter
@Setter
public class PinnacleConfig {
    
    private Api api = new Api();
    private Fetch fetch = new Fetch();

    @Getter
    @Setter
    public static class Api {
        private String baseUrl;
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class Fetch {
        private long intervalSeconds = 60;
    }
}
