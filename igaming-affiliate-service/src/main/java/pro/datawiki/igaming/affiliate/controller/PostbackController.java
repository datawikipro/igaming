package pro.datawiki.igaming.affiliate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.affiliate.model.AffiliateConversion;
import pro.datawiki.igaming.affiliate.service.AffiliateTrackingService;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/affiliate/postback")
@RequiredArgsConstructor
public class PostbackController {

    private final AffiliateTrackingService trackingService;

    @GetMapping("/{bookmaker}")
    public ResponseEntity<Map<String, Object>> handleGetPostback(
            @PathVariable("bookmaker") String bookmaker,
            @RequestParam("click_id") String clickId,
            @RequestParam(value = "conversion_type", defaultValue = "REGISTRATION") String conversionType,
            @RequestParam(value = "amount", required = false) BigDecimal amount,
            @RequestParam(value = "payout", required = false) BigDecimal payout,
            @RequestParam(value = "currency", defaultValue = "RUB") String currency,
            @RequestParam Map<String, String> allParams
    ) {
        AffiliateConversion conversion = trackingService.recordConversion(
                bookmaker,
                clickId,
                conversionType,
                amount,
                payout,
                currency,
                allParams.toString()
        );
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "conversionId", conversion.getId(),
                "clickId", conversion.getClickId()
        ));
    }

    @PostMapping("/{bookmaker}")
    public ResponseEntity<Map<String, Object>> handlePostPostback(
            @PathVariable("bookmaker") String bookmaker,
            @RequestBody Map<String, Object> body
    ) {
        String clickId = (String) body.getOrDefault("click_id", body.get("clickid"));
        if (clickId == null || clickId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing click_id"));
        }
        String conversionType = (String) body.getOrDefault("conversion_type", "REGISTRATION");
        BigDecimal amount = body.get("amount") != null ? new BigDecimal(body.get("amount").toString()) : null;
        BigDecimal payout = body.get("payout") != null ? new BigDecimal(body.get("payout").toString()) : null;
        String currency = (String) body.getOrDefault("currency", "RUB");

        AffiliateConversion conversion = trackingService.recordConversion(
                bookmaker,
                clickId,
                conversionType,
                amount,
                payout,
                currency,
                body.toString()
        );
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "conversionId", conversion.getId(),
                "clickId", conversion.getClickId()
        ));
    }
}
