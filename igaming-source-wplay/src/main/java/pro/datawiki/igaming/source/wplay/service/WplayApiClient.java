package pro.datawiki.igaming.source.wplay.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.wplay.config.WplayConfig;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class WplayApiClient {

    private final WplayConfig config;

    private volatile HttpClient httpClient;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    private HttpClient getHttpClient() {
        HttpClient client = this.httpClient;
        if (client == null) {
            synchronized (this) {
                client = this.httpClient;
                if (client == null) {
                    client = createHttpClient();
                    this.httpClient = client;
                }
            }
        }
        return client;
    }

    private HttpClient createHttpClient() {
        int connTimeoutMs = config.getConnectTimeout() > 0 ? config.getConnectTimeout() : 10000;
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofMillis(connTimeoutMs));

        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (proxyHost != null && !proxyHost.isBlank() && proxyPort != null && !proxyPort.isBlank()) {
            try {
                java.net.InetSocketAddress proxyAddr = new java.net.InetSocketAddress(proxyHost, Integer.parseInt(proxyPort));
                builder.proxy(new ProxySelector() {
                    @Override
                    public java.util.List<java.net.Proxy> select(URI uri) {
                        String host = uri.getHost();
                        if (host == null || host.equals("localhost") || host.equals("127.0.0.1")
                                || host.endsWith(".local") || host.endsWith(".internal")) {
                            return java.util.List.of(java.net.Proxy.NO_PROXY);
                        }
                        return java.util.List.of(new java.net.Proxy(java.net.Proxy.Type.HTTP, proxyAddr));
                    }

                    @Override
                    public void connectFailed(URI uri, java.net.SocketAddress sa, java.io.IOException ioe) {
                        log.warn("Proxy connection failed for {}: {}", uri, ioe.getMessage());
                    }
                });
                log.info("Wplay HttpClient configured with system proxy {}:{}", proxyHost, proxyPort);
            } catch (Exception e) {
                log.warn("Failed configuring system proxy {}:{} for Wplay: {}", proxyHost, proxyPort, e.getMessage());
            }
        }

        return builder.build();
    }

    public String fetchHtml(String pathOrUrl) {
        String url = pathOrUrl.startsWith("http") ? pathOrUrl : config.getBaseUrl() + pathOrUrl;
        long timeoutMs = config.getReadTimeout() > 0 ? config.getReadTimeout() : 15000;
        CompletableFuture<HttpResponse<String>> future = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                    .GET()
                    .build();

            future = getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> response = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            if (response.statusCode() == 200) {
                consecutiveFailures.set(0);
                return response.body();
            } else {
                log.warn("Wplay HTTP GET {} returned status {}", url, response.statusCode());
                handleFailure();
            }
        } catch (TimeoutException te) {
            log.warn("Timeout ({} ms) fetching HTML from Wplay url '{}'", timeoutMs, url);
            if (future != null) {
                future.cancel(true);
            }
            handleFailure();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted fetching HTML from Wplay url '{}'", url);
            if (future != null) {
                future.cancel(true);
            }
        } catch (java.util.concurrent.ExecutionException ee) {
            log.warn("Execution error fetching HTML from Wplay url '{}': {}", url, ee.getCause() != null ? ee.getCause().getMessage() : ee.getMessage());
            if (future != null) {
                future.cancel(true);
            }
            handleFailure();
        } catch (Exception e) {
            log.warn("Failed fetching HTML from Wplay url '{}': {}", url, e.getMessage());
            if (future != null) {
                future.cancel(true);
            }
            handleFailure();
        }
        return null;
    }

    private void handleFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= 5) {
            synchronized (this) {
                if (consecutiveFailures.get() >= 5) {
                    log.warn("Resetting Wplay HttpClient instance after {} consecutive failures/timeouts", failures);
                    this.httpClient = null;
                    consecutiveFailures.set(0);
                }
            }
        }
    }
}
