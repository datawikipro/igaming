package pro.datawiki.igaming.source.interwetten.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.interwetten")
@Data
public class InterwettenConfig {
    private String baseUrl;
    private int connectTimeout = 10000;
    private int readTimeout = 15000;
}