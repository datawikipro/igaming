package pro.datawiki.igaming.source.betvictor.config;

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
public class BetvictorConfig {

    @Value("${app.betvictor.base-url:https://www.betvictor.com}")
    private String baseUrl;

    @Value("${app.betvictor.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${app.betvictor.read-timeout:15000}")
    private int readTimeout;

    @Value("${app.betvictor.use-proxy:true}")
    private boolean useProxy;

    @Value("${app.betvictor.proxy-host:100.83.113.50}")
    private String proxyHost;

    @Value("${app.betvictor.proxy-port:3128}")
    private int proxyPort;

    @Bean
    public RestTemplate betvictorRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        if (useProxy && proxyHost != null && !proxyHost.isBlank()) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }

        return new RestTemplate(factory);
    }
}
