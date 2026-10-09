package pro.datawiki.igaming.source.tipsport.config;

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
public class TipsportConfig {

    @Value("${app.tipsport.base-url:https://www.tipsport.cz/rest/offer/v1}")
    private String baseUrl;

    @Value("${app.tipsport.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${app.tipsport.read-timeout:15000}")
    private int readTimeout;

    @Value("${app.tipsport.use-proxy:false}")
    private boolean useProxy;

    @Value("${app.tipsport.proxy-host:100.83.113.50}")
    private String proxyHost;

    @Value("${app.tipsport.proxy-port:3128}")
    private int proxyPort;

    @Bean
    public RestTemplate tipsportRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        if (useProxy && proxyHost != null && !proxyHost.isBlank()) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }

        return new RestTemplate(factory);
    }
}
