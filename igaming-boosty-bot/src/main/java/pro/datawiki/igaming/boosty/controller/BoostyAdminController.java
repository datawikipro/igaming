package pro.datawiki.igaming.boosty.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.boosty.model.BoostyDonor;
import pro.datawiki.igaming.boosty.model.BoostyDonorComment;
import pro.datawiki.igaming.boosty.model.BoostyDonorStatus;
import pro.datawiki.igaming.boosty.repository.BoostyDonorCommentRepository;
import pro.datawiki.igaming.boosty.service.BoostyDonorService;

import java.util.List;
import java.util.Map;

/**
 * Admin REST controller for Boosty Bot management.
 *
 * <p>Provides read-only endpoints for operations dashboard and manual triggers.
 * No authentication implemented here — intended for internal cluster use only
 * (not exposed via Ingress).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /admin/boosty/donors        — list all active donors</li>
 *   <li>GET  /admin/boosty/donors/all    — list all donors (any status)</li>
 *   <li>GET  /admin/boosty/donors/count  — count by status</li>
 *   <li>GET  /admin/boosty/comments      — list unforwarded donor comments</li>
 * </ul>
 */
@RestController
@RequestMapping("/admin/boosty")
@RequiredArgsConstructor
@Slf4j
public class BoostyAdminController {

    private final BoostyDonorService donorService;
    private final BoostyDonorCommentRepository commentRepository;
    private final pro.datawiki.igaming.boosty.service.BoostyApiClient apiClient;
    private final pro.datawiki.igaming.boosty.service.BoostyPublisherService publisherService;

    private static final String DISCLAIMER =
            "\n\n⚠️ Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно.";

    /** List all currently active Boosty donors. */
    @GetMapping("/donors")
    public ResponseEntity<List<BoostyDonor>> getActiveDonors() {
        return ResponseEntity.ok(donorService.getActiveDonors());
    }

    /** Count donors by status. */
    @GetMapping("/donors/count")
    public ResponseEntity<Map<String, Object>> getDonorCounts() {
        long active    = donorService.getActiveDonors().size();
        long pending   = commentRepository.findAllByForwardedToTelegramFalse().size();
        return ResponseEntity.ok(Map.of(
                "activeDonors", active,
                "pendingComments", pending
        ));
    }

    /** List all unforwarded donor comments. */
    @GetMapping("/comments")
    public ResponseEntity<List<BoostyDonorComment>> getPendingComments() {
        return ResponseEntity.ok(commentRepository.findAllByForwardedToTelegramFalse());
    }

    /** Check if a specific Boosty user ID is an active donor. */
    @GetMapping("/donors/{boostyUserId}/status")
    public ResponseEntity<Map<String, Object>> checkDonorStatus(
            @PathVariable Long boostyUserId) {
        return donorService.findByBoostyUserId(boostyUserId)
                .map(d -> ResponseEntity.ok(Map.<String, Object>of(
                        "boostyUserId", d.getBoostyUserId(),
                        "displayName", d.getDisplayName() != null ? d.getDisplayName() : "",
                        "status", d.getStatus().name(),
                        "amountRub", d.getAmountRub() != null ? d.getAmountRub() : 0,
                        "level", d.getSubscriptionLevel() != null ? d.getSubscriptionLevel() : ""
                )))
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "boostyUserId", boostyUserId,
                        "status", BoostyDonorStatus.EXPIRED.name()
                )));
    }

    /**
     * Publishes an exclusive post to Boosty.
     * Request body: { "title": "...", "content": "...", "teaser": "...", "min_tier_rub": 2500 }
     */
    @PostMapping("/posts")
    public ResponseEntity<Map<String, Object>> publishPost(@RequestBody Map<String, Object> body) {
        String title = body.get("title") != null ? body.get("title").toString() : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "content is required"));
        }
        String teaser = body.get("teaser") != null ? body.get("teaser").toString() : null;
        Integer minTierRub = null;
        if (body.containsKey("min_tier_rub")) {
            try {
                minTierRub = Integer.parseInt(body.get("min_tier_rub").toString());
            } catch (NumberFormatException ignored) {}
        }

        String fullContent = content.trim() + DISCLAIMER;
        Map<String, Object> result = apiClient.publishPost(title, fullContent, teaser, minTierRub);
        log.info("Boosty post published: title='{}', result={}", title, result);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "result", result
        ));
    }

    // ─────────────────────────────────────────────────────────
    // Subscription tiers
    // ─────────────────────────────────────────────────────────

    /**
     * Return all Boosty subscription tiers with prices and benefits.
     *
     * <p>Endpoint: GET /admin/boosty/tiers
     * Used by the igaming-admin-frontend dashboard and portal API.
     */
    @GetMapping("/tiers")
    public ResponseEntity<java.util.List<Map<String, Object>>> getSubscriptionTiers() {
        return ResponseEntity.ok(publisherService.getSubscriptionTiers());
    }

    // ─────────────────────────────────────────────────────────
    // Templated publishing
    // ─────────────────────────────────────────────────────────

    /**
     * Publish a freebet promotion post with automatic 80% guaranteed-cash calculation.
     *
     * <p>Endpoint: POST /admin/boosty/publish/freebet
     * Request body:
     * <pre>
     * {
     *   "bookmaker": "Фонбет",
     *   "freebet_amount_rub": 3000,
     *   "affiliate_url": "https://smartbet.guru/go/fonbet?utm_source=boosty"
     * }
     * </pre>
     *
     * <p>Per AGENTS.md Rule #10: guaranteed cash = freebet * 0.80 is computed and shown in the post.
     */
    @PostMapping("/publish/freebet")
    public ResponseEntity<Map<String, Object>> publishFreebetPromo(
            @RequestBody Map<String, Object> body) {
        String bookmaker = body.getOrDefault("bookmaker", "Букмекер").toString();
        int freebetAmt;
        try {
            freebetAmt = Integer.parseInt(body.getOrDefault("freebet_amount_rub", "0").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "freebet_amount_rub must be a number"));
        }
        if (freebetAmt <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "freebet_amount_rub must be > 0"));
        }
        String affiliateUrl = body.getOrDefault("affiliate_url",
                "https://smartbet.guru/go/" + bookmaker.toLowerCase()).toString();

        log.info("Boosty freebet promo: bookmaker='{}' amount={}₽ url={}",
                bookmaker, freebetAmt, affiliateUrl);
        Map<String, Object> result = publisherService.publishFreebetPromo(bookmaker, freebetAmt, affiliateUrl);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "guaranteed_cash_rub", (int) Math.round(freebetAmt * 0.80),
                "result", result
        ));
    }
}

