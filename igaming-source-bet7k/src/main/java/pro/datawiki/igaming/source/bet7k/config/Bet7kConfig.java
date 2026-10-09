package pro.datawiki.igaming.source.bet7k.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.bet7k")
public class Bet7kConfig {

    private String baseUrl = "https://sb-feed.altenar.com/api/Sportsbook";
    private String integrationId = "bet7k";
    private int connectTimeout = 10000;
    private int readTimeout = 15000;

    @Bean
    public RestTemplate bet7kRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return new RestTemplate(factory);
    }
}