package pro.datawiki.igaming.source.betman.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.betman")
@Data
public class BetmanConfig {
    private String baseUrl;
    private int connectTimeout = 10000;
    private int readTimeout = 15000;
}