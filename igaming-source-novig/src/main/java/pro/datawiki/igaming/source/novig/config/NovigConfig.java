package pro.datawiki.igaming.source.novig.config;

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

@Configuration
@ConfigurationProperties(prefix = "novig")
@Getter
@Setter
@Slf4j
public class NovigConfig {

    private Api api = new Api();
    private ProxyConfig proxy = new ProxyConfig();
    private Scheduler scheduler = new Scheduler();

    @Getter
    @Setter
    public static class Api {
        private String graphqlUrl = "https://api.novig.us/v1/graphql";
        private int connectTimeoutSeconds = 10;
        private int readTimeoutSeconds = 25;
        private int batchSize = 100;
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

    @Bean(name = "novigRestTemplate")
    public RestTemplate novigRestTemplate() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(api.getConnectTimeoutSeconds()))
                .setResponseTimeout(Timeout.ofSeconds(api.getReadTimeoutSeconds()))
                .build();

        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig);

        if (proxy.isEnabled() && proxy.getHost() != null && !proxy.getHost().isBlank()) {
            log.info("Configuring Novig RestTemplate with HTTP proxy {}:{}", proxy.getHost(), proxy.getPort());
            HttpHost proxyHost = new HttpHost("http", proxy.getHost(), proxy.getPort());
            clientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxyHost));
        } else {
            log.info("Configuring Novig RestTemplate with DIRECT connection");
        }

        CloseableHttpClient httpClient = clientBuilder.build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }
}
