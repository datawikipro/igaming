package pro.datawiki.igaming.source.nike.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.time.Duration;

@Configuration
public class NikeConfig {

    @Value("${app.nike.base-url:https://api.nike.sk}")
    private String baseUrl;

    @Value("${app.nike.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${app.nike.use-proxy:false}")
    private boolean useProxy;

    @Value("${app.nike.proxy-host:100.83.113.50}")
    private String proxyHost;

    @Value("${app.nike.proxy-port:3128}")
    private int proxyPort;

    @Bean
    public WebClient nikeWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(connectTimeout * 2L));

        if (useProxy && proxyHost != null && !proxyHost.isEmpty()) {
            httpClient = httpClient.proxy(proxy -> proxy
                    .type(ProxyProvider.Proxy.HTTP)
                    .address(new InetSocketAddress(proxyHost, proxyPort)));
        }

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .defaultHeader("Accept", "application/json, text/plain, */*")
                .defaultHeader("Accept-Language", "en-US,en;q=0.9")
                .build();
    }
}
