package pro.datawiki.igaming.source.smarkets.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "smarkets")
@Getter
@Setter
@Slf4j
public class SmarketsConfig {

    private Api api = new Api();
    private ProxyConfig proxy = new ProxyConfig();
    private Scheduler scheduler = new Scheduler();

    private List<String> sports = List.of(
            "football_match",
            "american_football_match",
            "tennis_match",
            "ice_hockey_match",
            "basketball_match",
            "baseball_match",
            "cricket_match",
            "mma_match",
            "boxing_match",
            "darts_match",
            "snooker_match",
            "rugby_union_match",
            "rugby_league_match",
            "table_tennis_match"
    );

    @Getter
    @Setter
    public static class Api {
        private String baseUrl = "https://api.smarkets.com/v3";
        private int connectTimeoutSeconds = 10;
        private int readTimeoutSeconds = 25;
        private int eventsPerSportLimit = 500;
    }

    @Getter
    @Setter
    public static class ProxyConfig {
        private boolean enabled = true;
        private String host = "100.83.113.50";
        private int port = 3128;
    }

    @Getter
    @Setter
    public static class Scheduler {
        private long pollIntervalMs = 30000;
    }

    @Bean(name = "smarketsRestTemplate")
    public RestTemplate smarketsRestTemplate() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(api.getConnectTimeoutSeconds()))
                .setResponseTimeout(Timeout.ofSeconds(api.getReadTimeoutSeconds()))
                .build();

        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig);

        if (proxy.isEnabled() && proxy.getHost() != null && !proxy.getHost().isBlank()) {
            log.info("Configuring Smarkets RestTemplate with HTTP proxy {}:{}", proxy.getHost(), proxy.getPort());
            HttpHost proxyHost = new HttpHost("http", proxy.getHost(), proxy.getPort());
            clientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxyHost));
        } else {
            log.info("Configuring Smarkets RestTemplate with DIRECT connection");
        }

        CloseableHttpClient httpClient = clientBuilder.build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }
}
