package pro.datawiki.igaming.source.bookmaker.config;

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
@ConfigurationProperties(prefix = "bookmaker")
@Getter
@Setter
@Slf4j
public class BookmakerConfig {

    private String baseUrl = "https://lines.bookmaker.eu";
    private List<String> sports = List.of(
            "baseball",
            "football",
            "basketball",
            "ice-hockey",
            "soccer",
            "tennis",
            "mma",
            "golf",
            "boxing",
            "cricket",
            "rugby",
            "table-tennis"
    );
    private ProxyConfig proxy = new ProxyConfig();

    @Getter
    @Setter
    public static class ProxyConfig {
        private boolean enabled = true;
        private String host = "100.83.113.50";
        private int port = 3128;
    }

    @Bean(name = "bookmakerRestTemplate")
    public RestTemplate bookmakerRestTemplate() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(10))
                .setResponseTimeout(Timeout.ofSeconds(25))
                .build();

        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig);

        if (proxy.isEnabled() && proxy.getHost() != null && !proxy.getHost().isBlank()) {
            log.info("Configuring BookMaker RestTemplate with HTTP proxy {}:{}", proxy.getHost(), proxy.getPort());
            HttpHost proxyHost = new HttpHost("http", proxy.getHost(), proxy.getPort());
            clientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxyHost));
        } else {
            log.info("Configuring BookMaker RestTemplate with DIRECT connection");
        }

        CloseableHttpClient httpClient = clientBuilder.build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }
}
