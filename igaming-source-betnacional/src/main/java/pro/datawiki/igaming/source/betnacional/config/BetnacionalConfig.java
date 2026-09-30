package pro.datawiki.igaming.source.betnacional.config;

import lombok.Data;
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
@ConfigurationProperties(prefix = "app.betnacional")
public class BetnacionalConfig {

    private String apiUrl = "https://betnacional.com";
    private boolean useProxy = true;
    private String proxyHost = "100.83.113.50";
    private int proxyPort = 3128;
    private long pollRateMs = 10000;

    private List<String> sports = Arrays.asList(
            "soccer", "basketball", "tennis", "ice-hockey", "esports",
            "table-tennis", "volleyball", "baseball", "handball", "mma"
    );

    @Bean(name = "betnacionalRestTemplate")
    public RestTemplate betnacionalRestTemplate() {
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
