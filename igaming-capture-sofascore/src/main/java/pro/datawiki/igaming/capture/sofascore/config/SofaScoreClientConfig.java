package pro.datawiki.igaming.capture.sofascore.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.util.SofaScoreHeaderUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;

/**
 * Spring configuration providing two isolated HTTP clients:
 * 1. Default (Primary) RestTemplate: direct connection for internal cluster communications (aggregator-core, K8s DNS).
 * 2. sofaScoreRestTemplate: proxied through cluster-switch-proxy.proxy:3128 with full browser fingerprint emulation
 *    to bypass Varnish and Cloudflare 403 Forbidden protection.
 */
@Configuration
@Slf4j
public class SofaScoreClientConfig {

    @Value("${sofascore.proxy.enabled:true}")
    private boolean proxyEnabled;

    @Value("${sofascore.proxy.host:cluster-switch-proxy.proxy}")
    private String proxyHost;

    @Value("${sofascore.proxy.port:3128}")
    private int proxyPort;

    @Value("${app.capture.user-agent:" + SofaScoreHeaderUtils.DEFAULT_USER_AGENT + "}")
    private String userAgent;

    /**
     * Internal unproxied RestTemplate for aggregator-core and local K8s services.
     */
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
    }

    @Value("${sofascore.api.base-url:}")
    private String apiBaseUrl;

    /**
     * Dedicated external RestTemplate for api.sofascore.com calls.
     * Routes traffic through internal bridge if configured or cluster proxy when enabled,
     * injecting realistic desktop browser headers.
     */
    @Bean(name = "sofaScoreRestTemplate")
    public RestTemplate sofaScoreRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(15000);

        if (proxyEnabled && proxyHost != null && !proxyHost.isBlank()) {
            log.info("[SofaScore Client] Routing external calls via HTTP proxy {}:{}", proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            factory.setProxy(proxy);
        } else {
            log.info("[SofaScore Client] External proxy disabled or host empty. Using direct connection.");
        }

        RestTemplate template = new RestTemplate(factory);
        java.util.List<ClientHttpRequestInterceptor> interceptors = new java.util.ArrayList<>();

        if (apiBaseUrl != null && !apiBaseUrl.isBlank()) {
            log.info("[SofaScore Client] Routing SofaScore requests via bridge: {}", apiBaseUrl);
            java.net.URI baseUri = java.net.URI.create(apiBaseUrl);
            interceptors.add((request, body, execution) -> {
                java.net.URI originalUri = request.getURI();
                if (originalUri.getHost() != null && originalUri.getHost().contains("sofascore.com")) {
                    java.net.URI targetUri = org.springframework.web.util.UriComponentsBuilder.fromUri(originalUri)
                            .scheme(baseUri.getScheme())
                            .host(baseUri.getHost())
                            .port(baseUri.getPort())
                            .build(true)
                            .toUri();
                    org.springframework.http.HttpRequest modifiedRequest = new org.springframework.http.client.support.HttpRequestWrapper(request) {
                        @Override
                        public java.net.URI getURI() {
                            return targetUri;
                        }
                    };
                    return execution.execute(modifiedRequest, body);
                }
                return execution.execute(request, body);
            });
        }

        ClientHttpRequestInterceptor browserInterceptor = (request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            SofaScoreHeaderUtils.applyBrowserHeaders(headers, userAgent);
            return execution.execute(request, body);
        };
        interceptors.add(browserInterceptor);

        template.setInterceptors(interceptors);
        return template;
    }
}
