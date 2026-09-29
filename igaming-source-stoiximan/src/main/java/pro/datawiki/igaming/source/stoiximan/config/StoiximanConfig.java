package pro.datawiki.igaming.source.stoiximan.config;

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
@ConfigurationProperties(prefix = "stoiximan")
@Getter
@Setter
@Slf4j
public class StoiximanConfig {

    private Api api = new Api();
    private ProxyConfig proxy = new ProxyConfig();
    private List<String> sports = List.of(
            "football",
            "tennis",
            "basketball",
            "ice_hockey",
            "baseball",
            "esports",
            "volleyball",
            "handball",
            "table_tennis",
            "cricket",
            "golf",
            "boxing",
            "motorsports",
            "american_football",
            "darts",
            "snooker"
    );

    @Getter
    @Setter
    public static class Api {
        private String baseUrl = "https://eu.offering-api.kambicdn.com/offering/v2018";
        private String brand = "kambi";
        private String locale = "en_GB";
        private String market = "GLOBAL";
        private int connectTimeoutSeconds = 10;
        private int readTimeoutSeconds = 25;
    }

    @Getter
    @Setter
    public static class ProxyConfig {
        private boolean enabled = true;
        private String host = "proxy-us.service-proxy.svc.cluster.local";
        private int port = 31292;
    }

    @Bean(name = "stoiximanRestTemplate")
    public RestTemplate stoiximanRestTemplate() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(api.getConnectTimeoutSeconds()))
                .setResponseTimeout(Timeout.ofSeconds(api.getReadTimeoutSeconds()))
                .build();

        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig);

        if (proxy.isEnabled() && proxy.getHost() != null && !proxy.getHost().isBlank()) {
            log.info("Configuring Stoiximan RestTemplate with HTTP proxy {}:{}", proxy.getHost(), proxy.getPort());
            HttpHost proxyHost = new HttpHost("http", proxy.getHost(), proxy.getPort());
            clientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxyHost));
        } else {
            log.info("Configuring Stoiximan RestTemplate with DIRECT connection");
        }

        CloseableHttpClient httpClient = clientBuilder.build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }
}
