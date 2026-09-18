package pro.datawiki.igaming.source.bovada.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "bovada")
@Getter
@Setter
@Slf4j
public class BovadaConfig {

    private Api api = new Api();
    private ProxyConfig proxy = new ProxyConfig();
    private List<String> sports = Arrays.asList("football", "basketball", "soccer", "hockey", "baseball", "tennis", "ufc-mma");

    @Getter
    @Setter
    public static class Api {
        private String baseUrl = "https://www.bovada.lv/services/sports/event/coupon/events/A/description";
        private int connectTimeoutSeconds = 10;
        private int readTimeoutSeconds = 30;
    }

    @Getter
    @Setter
    public static class ProxyConfig {
        private boolean enabled = true;
        private String host = "100.83.113.50";
        private int port = 3128;
    }

    @Bean(name = "bovadaRestTemplate")
    public RestTemplate bovadaRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(api.getConnectTimeoutSeconds()).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(api.getReadTimeoutSeconds()).toMillis());

        if (proxy.isEnabled() && proxy.getHost() != null && !proxy.getHost().isBlank()) {
            log.info("Configuring Bovada RestTemplate with HTTP proxy {}:{}", proxy.getHost(), proxy.getPort());
            Proxy httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
            factory.setProxy(httpProxy);
        } else {
            log.info("Configuring Bovada RestTemplate with DIRECT connection");
        }

        return new RestTemplate(factory);
    }
}
