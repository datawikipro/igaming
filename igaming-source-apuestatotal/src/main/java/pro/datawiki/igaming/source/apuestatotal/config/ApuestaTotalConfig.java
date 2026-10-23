package pro.datawiki.igaming.source.apuestatotal.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.altenar")
public class ApuestaTotalConfig {

    @Value("${app.altenar.base-url:${app.apuestatotal.base-url:https://sb2frontend-altenar2.biahosted.com/api}}")
    private String baseUrl = "https://sb2frontend-altenar2.biahosted.com/api";

    @Value("${app.altenar.integration-id:${app.apuestatotal.integration-id:${app.bookmaker.name:apuestatotal}}}")
    private String integrationId = "apuestatotal";

    @Value("${app.altenar.origin:}")
    private String origin;

    @Value("${app.altenar.culture:en-GB}")
    private String culture = "en-GB";

    @Value("${app.altenar.country-code:BR}")
    private String countryCode = "BR";

    @Value("${app.altenar.timezone-offset:180}")
    private int timezoneOffset = 180;

    @Value("${app.altenar.connect-timeout:15000}")
    private int connectTimeout = 15000;

    @Value("${app.altenar.read-timeout:120000}")
    private int readTimeout = 120000;

    @Value("${app.altenar.proxy.enabled:${app.proxy.enabled:false}}")
    private boolean useProxy;

    @Value("${app.altenar.proxy.host:${app.proxy.host:purevpn-jp-direct.proxy.svc.cluster.local}}")
    private String proxyHost = "purevpn-jp-direct.proxy.svc.cluster.local";

    @Value("${app.altenar.proxy.port:${app.proxy.port:3128}}")
    private int proxyPort = 3128;

    @Bean
    public RestTemplate apuestatotalRestTemplate() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .followRedirects(HttpClient.Redirect.NORMAL);

        if (useProxy && proxyHost != null && !proxyHost.isBlank()) {
            builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
        }

        HttpClient httpClient = builder.build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return new RestTemplate(factory);
    }
}
