package pro.datawiki.igaming.b2b.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.b2b.service.B2bQuotaService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quota")
@RequiredArgsConstructor
public class B2BQuotaController {

    private final B2bQuotaService quotaService;

    @GetMapping
    public ResponseEntity<?> getQuotaStatus(@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing X-API-Key header"));
        }

        if (!quotaService.validateApiKey(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Invalid or expired API Key"));
        }

        int limit = quotaService.getLimitForApiKey(apiKey);
        int remaining = quotaService.getRemaining(apiKey, limit);

        return ResponseEntity.ok(Map.of(
                "daily_limit", limit,
                "remaining_today", remaining,
                "used_today", limit - remaining,
                "reset_date", LocalDate.now().plusDays(1).toString()
        ));
    }
}
