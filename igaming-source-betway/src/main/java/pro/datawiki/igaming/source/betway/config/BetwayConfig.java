package pro.datawiki.igaming.source.betway.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
@Data
public class BetwayConfig {

    @Value("${app.betway.base-url:https://api.betway.com/v1/sportsbook}")
    private String baseUrl;

    @Value("${app.betway.brand:betway}")
    private String brand;

    @Value("${app.betway.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${app.betway.read-timeout:15000}")
    private int readTimeout;

    @Value("${app.betway.use-proxy:false}")
    private boolean useProxy;

    @Value("${app.betway.proxy-host:100.83.113.50}")
    private String proxyHost;

    @Value("${app.betway.proxy-port:3128}")
    private int proxyPort;

    @Bean
    public RestTemplate betwayRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        if (useProxy && proxyHost != null && !proxyHost.isBlank()) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }

        return new RestTemplate(factory);
    }
}
