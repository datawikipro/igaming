package pro.datawiki.igaming.source.wplay.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.source.wplay.config.WplayConfig;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class WplayApiClientTest {

    private HttpServer testServer;
    private int testPort;
    private WplayConfig config;
    private WplayApiClient apiClient;

    @BeforeEach
    void setUp() throws Exception {
        testServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        testPort = testServer.getAddress().getPort();

        config = new WplayConfig();
        config.setBaseUrl("http://127.0.0.1:" + testPort);
        config.setConnectTimeout(2000);
        config.setReadTimeout(1000);

        apiClient = new WplayApiClient(config);
    }

    @AfterEach
    void tearDown() {
        if (testServer != null) {
            testServer.stop(0);
        }
    }

    @Test
    void testFetchHtmlSuccess() {
        String mockHtml = "<html><body><a href=\"/es/e/12345/TeamA-v-TeamB\">Match</a></body></html>";
        testServer.createContext("/es", exchange -> {
            byte[] responseBytes = mockHtml.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });
        testServer.start();

        String html = apiClient.fetchHtml("/es");
        assertNotNull(html);
        assertTrue(html.contains("TeamA-v-TeamB"));
    }

    @Test
    void testFetchHtmlTimeoutReturnsNullWithoutHanging() {
        testServer.createContext("/slow", exchange -> {
            try {
                // Sleep longer than config.readTimeout (1000ms)
                Thread.sleep(6000);
            } catch (InterruptedException ignored) {}
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });
        testServer.start();

        long start = System.currentTimeMillis();
        String html = apiClient.fetchHtml("/slow");
        long elapsed = System.currentTimeMillis() - start;

        assertNull(html, "Slow response must return null on timeout");
        assertTrue(elapsed < 5000, "Must timeout promptly around 1000ms, elapsed was: " + elapsed + "ms");
    }

    @Test
    void testFetchHtmlHttpErrorReturnsNull() {
        testServer.createContext("/error", exchange -> {
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        });
        testServer.start();

        String html = apiClient.fetchHtml("/error");
        assertNull(html);
    }
}
