package pro.datawiki.igaming.source.superbet.config;

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
public class SuperbetConfig {

    @Value("${app.superbet.base-url:https://production-superbet-offer-pl.freetls.fastly.net/v3/pl-PL}")
    private String baseUrl;

    @Value("${app.superbet.connect-timeout:15000}")
    private int connectTimeout;

    @Value("${app.superbet.read-timeout:20000}")
    private int readTimeout;

    @Value("${app.superbet.use-proxy:true}")
    private boolean useProxy;

    @Value("${app.superbet.proxy-host:hk-proxy.proxy.svc.cluster.local}")
    private String proxyHost;

    @Value("${app.superbet.proxy-port:3128}")
    private int proxyPort;

    @Bean
    public RestTemplate superbetRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        if (useProxy && proxyHost != null && !proxyHost.isBlank()) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }

        return new RestTemplate(factory);
    }
}
