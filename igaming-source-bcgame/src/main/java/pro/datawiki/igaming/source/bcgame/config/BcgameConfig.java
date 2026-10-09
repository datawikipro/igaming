package pro.datawiki.igaming.source.bcgame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.bcgame")
public class BcgameConfig {

    private String baseUrl = "https://bc.game/api/sports";
    private int connectTimeout = 10000;
    private int readTimeout = 15000;
    private boolean useProxy = true;
    private String proxyHost = "100.83.113.50";
    private int proxyPort = 3128;
    private long fetchIntervalMs = 30000;

    @Bean
    public RestTemplate bcgameRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        if (useProxy && proxyHost != null && !proxyHost.isBlank()) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }
        return new RestTemplate(factory);
    }
}
