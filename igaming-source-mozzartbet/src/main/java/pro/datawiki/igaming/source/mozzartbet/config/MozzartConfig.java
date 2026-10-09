package pro.datawiki.igaming.source.mozzartbet.config;

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
public class MozzartConfig {

    @Value("${app.mozzartbet.base-url:https://www.mozzartbet.com/api/v1}")
    private String baseUrl;

    @Value("${app.mozzartbet.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${app.mozzartbet.read-timeout:15000}")
    private int readTimeout;

    @Value("${app.mozzartbet.use-proxy:false}")
    private boolean useProxy;

    @Value("${app.mozzartbet.proxy-host:100.83.113.50}")
    private String proxyHost;

    @Value("${app.mozzartbet.proxy-port:3128}")
    private int proxyPort;

    @Bean
    public RestTemplate mozzartRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        if (useProxy && proxyHost != null && !proxyHost.isBlank()) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }

        return new RestTemplate(factory);
    }
}
