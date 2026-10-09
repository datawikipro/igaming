package pro.datawiki.igaming.source.vaidebet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.vaidebet")
public class VaidebetConfig {

    private String baseUrl = "https://sb-feed.altenar.com/api/Sportsbook";
    private String integrationId = "vaidebet";
    private int connectTimeout = 10000;
    private int readTimeout = 15000;

    @Bean
    public RestTemplate vaidebetRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return new RestTemplate(factory);
    }
}