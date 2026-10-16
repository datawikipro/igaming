package pro.datawiki.igaming.capture.sofascore.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.util.SofaScoreHeaderUtils;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SofaScoreClientConfigTest {

    @Test
    @DisplayName("Primary restTemplate must be direct without proxy or browser interceptors")
    void testPrimaryRestTemplateIsDirect() {
        SofaScoreClientConfig config = new SofaScoreClientConfig();
        RestTemplate directTemplate = config.restTemplate();

        assertThat(directTemplate).isNotNull();
        assertThat(directTemplate.getInterceptors()).isEmpty();
    }

    @Test
    @DisplayName("sofaScoreRestTemplate must have browser headers interceptor configured")
    void testSofaScoreRestTemplateHasBrowserInterceptor() throws IOException {
        SofaScoreClientConfig config = new SofaScoreClientConfig();
        ReflectionTestUtils.setField(config, "proxyEnabled", false);
        ReflectionTestUtils.setField(config, "proxyHost", "cluster-switch-proxy.proxy");
        ReflectionTestUtils.setField(config, "proxyPort", 3128);
        ReflectionTestUtils.setField(config, "userAgent", SofaScoreHeaderUtils.DEFAULT_USER_AGENT);

        RestTemplate sofaTemplate = config.sofaScoreRestTemplate();
        assertThat(sofaTemplate).isNotNull();

        List<ClientHttpRequestInterceptor> interceptors = sofaTemplate.getInterceptors();
        assertThat(interceptors).hasSize(1);

        ClientHttpRequestInterceptor interceptor = interceptors.get(0);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("https://api.sofascore.com/api/v1/test"));
        byte[] body = new byte[0];

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse mockResponse = new MockClientHttpResponse(new byte[0], 200);
        when(execution.execute(any(ClientHttpRequest.class), any(byte[].class))).thenReturn(mockResponse);

        interceptor.intercept(request, body, execution);

        HttpHeaders headers = request.getHeaders();
        assertThat(headers.getFirst(HttpHeaders.USER_AGENT)).contains("Mozilla/5.0");
        assertThat(headers.getFirst("sec-ch-ua")).contains("Chromium");
        assertThat(headers.getFirst("sec-ch-ua-mobile")).isEqualTo("?0");
        assertThat(headers.getFirst("sec-ch-ua-platform")).isEqualTo("\"Windows\"");
        assertThat(headers.getFirst(HttpHeaders.REFERER)).isEqualTo("https://www.sofascore.com/");
        assertThat(headers.getFirst(HttpHeaders.ORIGIN)).isEqualTo("https://www.sofascore.com");
        assertThat(headers.getFirst(HttpHeaders.ACCEPT_LANGUAGE)).contains("ru,en;q=0.9,de;q=0.8");
    }

    @Test
    @DisplayName("sofaScoreRestTemplate proxy configuration when enabled")
    void testSofaScoreRestTemplateProxyEnabled() {
        SofaScoreClientConfig config = new SofaScoreClientConfig();
        ReflectionTestUtils.setField(config, "proxyEnabled", true);
        ReflectionTestUtils.setField(config, "proxyHost", "cluster-switch-proxy.proxy");
        ReflectionTestUtils.setField(config, "proxyPort", 3128);
        ReflectionTestUtils.setField(config, "userAgent", SofaScoreHeaderUtils.DEFAULT_USER_AGENT);

        RestTemplate sofaTemplate = config.sofaScoreRestTemplate();
        assertThat(sofaTemplate).isNotNull();
    }
}
