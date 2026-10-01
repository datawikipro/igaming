package pro.datawiki.igaming.source.caliente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.caliente.config.CalienteConfig;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalienteApiClient {

    private final CalienteConfig config;

    private HttpClient createHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(10));

        if (config.isUseProxy() && config.getProxyHost() != null && !config.getProxyHost().isBlank()) {
            builder.proxy(ProxySelector.of(new InetSocketAddress(config.getProxyHost(), config.getProxyPort())));
        }

        return builder.build();
    }

    public String fetchHtml(String pathOrUrl) {
        String url = pathOrUrl.startsWith("http") ? pathOrUrl : config.getBaseUrl() + pathOrUrl;
        try {
            HttpClient client = createHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "es-MX,es;q=0.9,en;q=0.8")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.warn("Caliente HTTP GET {} returned status {}", url, response.statusCode());
            }
        } catch (Exception e) {
            log.warn("Failed fetching HTML from Caliente url '{}': {}", url, e.getMessage());
        }
        return null;
    }
}
