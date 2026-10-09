package pro.datawiki.igaming.source.winner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.winner")
@Data
public class WinnerConfig {
    private String baseUrl;
    private int connectTimeout = 10000;
    private int readTimeout = 15000;
}