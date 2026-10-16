package pro.datawiki.igaming.capture.sofascore.util;

import org.springframework.http.HttpHeaders;

/**
 * Standard HTTP header definitions for SofaScore requests.
 * Emulates a real desktop browser profile to prevent 403 Forbidden
 * blocks by Cloudflare and Varnish WAF.
 */
public final class SofaScoreHeaderUtils {

    public static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
    public static final String SEC_CH_UA =
            "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"";
    public static final String SEC_CH_UA_MOBILE = "?0";
    public static final String SEC_CH_UA_PLATFORM = "\"Windows\"";
    public static final String REFERER = "https://www.sofascore.com/";
    public static final String ORIGIN = "https://www.sofascore.com";
    public static final String ACCEPT_LANGUAGE = "ru,en;q=0.9,de;q=0.8";
    public static final String ACCEPT = "application/json, text/plain, */*";

    private SofaScoreHeaderUtils() {}

    public static HttpHeaders createBrowserHeaders() {
        return createBrowserHeaders(DEFAULT_USER_AGENT);
    }

    public static HttpHeaders createBrowserHeaders(String userAgent) {
        HttpHeaders headers = new HttpHeaders();
        applyBrowserHeaders(headers, userAgent);
        return headers;
    }

    public static void applyBrowserHeaders(HttpHeaders headers, String userAgent) {
        headers.set(HttpHeaders.USER_AGENT, (userAgent != null && !userAgent.isBlank()) ? userAgent : DEFAULT_USER_AGENT);
        headers.set("sec-ch-ua", SEC_CH_UA);
        headers.set("sec-ch-ua-mobile", SEC_CH_UA_MOBILE);
        headers.set("sec-ch-ua-platform", SEC_CH_UA_PLATFORM);
        headers.set(HttpHeaders.REFERER, REFERER);
        headers.set(HttpHeaders.ORIGIN, ORIGIN);
        headers.set(HttpHeaders.ACCEPT_LANGUAGE, ACCEPT_LANGUAGE);
        headers.set(HttpHeaders.ACCEPT, ACCEPT);
    }
}
