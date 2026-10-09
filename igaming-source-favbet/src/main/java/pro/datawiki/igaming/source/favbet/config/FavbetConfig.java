package pro.datawiki.igaming.source.favbet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.favbet")
public class FavbetConfig {

    private String baseUrl = "https://www.favbet.com/frontend_api/frontend";
    private int connectTimeout = 10000;
    private int readTimeout = 15000;
    private boolean useProxy = false;
    private String proxyHost = "100.83.113.50";
    private int proxyPort = 3128;

    @Bean
    public RestTemplate favbetRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        if (useProxy && proxyHost != null && !proxyHost.isBlank()) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }
        return new RestTemplate(factory);
    }
}
