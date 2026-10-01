package pro.datawiki.igaming.source.tenbet.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.10bet")
public class TenBetConfig {

    @Value("${APP_10BET_API_URL:${app.10bet.api-url:https://www.10bet.com}}")
    private String apiUrl = "https://www.10bet.com";

    @Value("${APP_10BET_USE_PROXY:${app.10bet.use-proxy:true}}")
    private boolean useProxy = true;

    @Value("${APP_10BET_PROXY_HOST:${app.10bet.proxy-host:100.83.113.50}}")
    private String proxyHost = "100.83.113.50";

    @Value("${APP_10BET_PROXY_PORT:${app.10bet.proxy-port:3128}}")
    private int proxyPort = 3128;

    @Value("${APP_10BET_POLL_RATE_MS:${app.10bet.poll-rate-ms:10000}}")
    private long pollRateMs = 10000;

    private List<String> sports = Arrays.asList(
            "football", "basketball", "tennis", "ice-hockey", "esports",
            "table-tennis", "volleyball", "baseball", "handball", "mma"
    );

    @Bean(name = "tenBetRestTemplate")
    public RestTemplate tenBetRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        if (useProxy && proxyHost != null && !proxyHost.isBlank() && proxyPort > 0) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            factory.setProxy(proxy);
        }

        return new RestTemplate(factory);
    }
}
