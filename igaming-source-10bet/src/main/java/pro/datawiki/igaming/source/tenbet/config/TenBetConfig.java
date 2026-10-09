package pro.datawiki.igaming.source.tenbet.config;

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
public class TenBetConfig {

    @Value("${app.10bet.base-url:https://api.10bet.com/v1/sportsbook}")
    private String baseUrl;

    @Value("${app.10bet.brand:10bet}")
    private String brand;

    @Value("${app.10bet.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${app.10bet.read-timeout:15000}")
    private int readTimeout;

    @Value("${app.10bet.use-proxy:false}")
    private boolean useProxy;

    @Value("${app.10bet.proxy-host:100.83.113.50}")
    private String proxyHost;

    @Value("${app.10bet.proxy-port:3128}")
    private int proxyPort;

    @Bean
    public RestTemplate tenBetRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        if (useProxy && proxyHost != null && !proxyHost.isBlank()) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }

        return new RestTemplate(factory);
    }
}
