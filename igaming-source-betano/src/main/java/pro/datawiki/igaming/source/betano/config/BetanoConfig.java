package pro.datawiki.igaming.source.betano.config;

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
public class BetanoConfig {

    @Value("${app.betano.base-url:https://www.betano.com}")
    private String baseUrl;

    @Value("${app.betano.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${app.betano.read-timeout:15000}")
    private int readTimeout;

    @Value("${app.betano.use-proxy:true}")
    private boolean useProxy;

    @Value("${app.betano.proxy-host:100.83.113.50}")
    private String proxyHost;

    @Value("${app.betano.proxy-port:3128}")
    private int proxyPort;

    @Bean
    public RestTemplate betanoRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        if (useProxy && proxyHost != null && !proxyHost.isBlank()) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }

        return new RestTemplate(factory);
    }
}
