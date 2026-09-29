package pro.datawiki.igaming.affiliate.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.affiliate.service.AffiliateTrackingService;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final AffiliateTrackingService trackingService;

    @GetMapping("/go/{bookmaker}")
    public ResponseEntity<Void> redirectBookmaker(
            @PathVariable("bookmaker") String bookmaker,
            @RequestParam Map<String, String> queryParams,
            HttpServletRequest request
    ) {
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String referer = request.getHeader(HttpHeaders.REFERER);

        String targetUrl = trackingService.generateRedirectUrl(
                bookmaker,
                queryParams,
                clientIp,
                userAgent,
                referer
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(targetUrl));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/r/{code}")
    public ResponseEntity<Void> redirectShortCode(
            @PathVariable("code") String code,
            @RequestParam Map<String, String> queryParams,
            HttpServletRequest request
    ) {
        return redirectBookmaker(code, queryParams, request);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String cfIp = request.getHeader("CF-Connecting-IP");
        if (cfIp != null && !cfIp.isBlank()) {
            return cfIp.trim();
        }
        return request.getRemoteAddr();
    }
}
