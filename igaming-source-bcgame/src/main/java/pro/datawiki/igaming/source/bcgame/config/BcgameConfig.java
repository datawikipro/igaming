package pro.datawiki.igaming.source.bcgame.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
@ConfigurationProperties(prefix = "app.bcgame")
@Data
@Slf4j
public class BcgameConfig {

    private String baseUrl = "https://bc.game/api/sports";
    private int connectTimeout = 5000;
    private int readTimeout = 10000;
    private boolean useProxy = false;
    private String proxyHost = "100.83.113.50";
    private int proxyPort = 3128;
    private long fetchIntervalMs = 30000;

    @Bean
    public RestTemplate bcgameRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        if (useProxy && proxyHost != null && !proxyHost.isBlank() && proxyPort > 0) {
            log.info("Configuring BC.Game RestTemplate with HTTP proxy {}:{}", proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            factory.setProxy(proxy);
        } else {
            log.info("Configuring BC.Game RestTemplate with direct connection");
        }

        return new RestTemplate(factory);
    }
}
