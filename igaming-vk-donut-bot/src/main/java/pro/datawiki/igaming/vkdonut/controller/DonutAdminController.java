package pro.datawiki.igaming.vkdonut.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.vkdonut.model.DonutDonor;
import pro.datawiki.igaming.vkdonut.service.DonutDonorService;
import pro.datawiki.igaming.vkdonut.service.DonutSignalBroadcaster;
import pro.datawiki.igaming.vkdonut.service.VkCommunityWallPoster;

import java.util.List;
import java.util.Map;

/**
 * Admin REST API for monitoring and manual operations on VK Donut donors
 * and the public VK community wall (SmartBet.guru).
 *
 * Endpoints (internal, not exposed to VK):
 *   GET  /admin/donut/donors              — list all active Donut donors
 *   POST /admin/donut/broadcast           — send manual broadcast to all active donors
 *   POST /admin/donut/posts               — publish exclusive Donut wall post
 *   POST /admin/community/posts           — publish public community wall post
 *   POST /admin/community/freebet-post    — publish freebet promo post (80% cash math)
 *   POST /admin/community/signal-trigger  — manually trigger surebet signal post
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class DonutAdminController {

    private final DonutDonorService donorService;
    private final DonutSignalBroadcaster broadcaster;
    private final VkCommunityWallPoster communityWallPoster;

    // =====================================================================
    // VK Donut — Exclusive donor operations
    // =====================================================================

    /**
     * List all active Donut donors.
     */
    @GetMapping("/admin/donut/donors")
    public ResponseEntity<List<DonutDonor>> listActiveDonors() {
        List<DonutDonor> donors = donorService.getActiveDonors();
        return ResponseEntity.ok(donors);
    }

    /**
     * Manually trigger a broadcast message to all active Donut donors.
     *
     * Request body: {"text": "Your message here"}
     */
    @PostMapping("/admin/donut/broadcast")
    public ResponseEntity<Map<String, Object>> broadcastMessage(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "text is required"));
        }
        int activeCount = donorService.getActiveDonors().size();
        broadcaster.broadcastAdminMessage(text);
        log.info("Admin broadcast triggered: '{}' to {} donor(s).", text, activeCount);
        return ResponseEntity.ok(Map.of(
                "status", "sent",
                "recipients", activeCount
        ));
    }

    /**
     * Publishes an exclusive post to the VK community wall for Donut supporters.
     *
     * Request body: { "title": "...", "content": "...", "donut_paid_duration": -1 }
     */
    @PostMapping("/admin/donut/posts")
    public ResponseEntity<Map<String, Object>> publishDonutPost(@RequestBody Map<String, Object> body) {
        String title = body.get("title") != null ? body.get("title").toString() : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "content is required"));
        }

        int donutPaidDuration = -1;
        if (body.containsKey("donut_paid_duration")) {
            try {
                donutPaidDuration = Integer.parseInt(body.get("donut_paid_duration").toString());
            } catch (NumberFormatException ignored) {}
        }

        Map<String, Object> result = broadcaster.publishExclusivePost(title, content, donutPaidDuration);
        log.info("VK Donut wall post triggered: title='{}', result={}", title, result);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "donut_paid_duration", donutPaidDuration,
                "result", result
        ));
    }

    // =====================================================================
    // VK Community — Public wall posts (visible to all community members)
    // =====================================================================

    /**
     * Publishes a custom public post to the VK community wall.
     * Disclaimer is always appended.
     *
     * Request body: { "title": "...", "content": "..." }
     */
    @PostMapping("/admin/community/posts")
    public ResponseEntity<Map<String, Object>> publishCommunityPost(@RequestBody Map<String, Object> body) {
        String title   = body.get("title")   != null ? body.get("title").toString()   : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "content is required"));
        }
        Map<String, Object> result = communityWallPoster.publishCustomPost(title, content);
        log.info("VK Community public wall post published: title='{}', result={}", title, result);
        return ResponseEntity.ok(Map.of("status", "success", "result", result));
    }

    /**
     * Publishes a freebet promo post to the public VK community wall.
     * Calculates 80% guaranteed cash conversion and appends affiliate + calculator links.
     *
     * Request body: { "bookmaker_name": "Фонбет", "freebet_amount_rub": 3000, "affiliate_slug": "fonbet" }
     */
    @PostMapping("/admin/community/freebet-post")
    public ResponseEntity<Map<String, Object>> publishFreebetPost(@RequestBody Map<String, Object> body) {
        String bookmakerName  = body.get("bookmaker_name")  != null ? body.get("bookmaker_name").toString()  : null;
        String affiliateSlug  = body.get("affiliate_slug")  != null ? body.get("affiliate_slug").toString()  : null;
        if (bookmakerName == null || bookmakerName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "bookmaker_name is required"));
        }
        if (affiliateSlug == null || affiliateSlug.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "affiliate_slug is required"));
        }
        int freebetAmount = 0;
        try {
            freebetAmount = Integer.parseInt(body.getOrDefault("freebet_amount_rub", "0").toString());
        } catch (NumberFormatException ignored) {}
        if (freebetAmount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "freebet_amount_rub must be a positive integer"));
        }

        Map<String, Object> result = communityWallPoster.publishFreebetPost(bookmakerName, freebetAmount, affiliateSlug);
        log.info("VK Community freebet post: bk='{}', amount={}₽, result={}", bookmakerName, freebetAmount, result);
        return ResponseEntity.ok(Map.of("status", "success", "guaranteed_cash_80pct", (int)(freebetAmount * 0.80), "result", result));
    }

    /**
     * Manually triggers an immediate public surebet signal post to the community wall.
     * Useful for testing or on-demand publishing.
     */
    @PostMapping("/admin/community/signal-trigger")
    public ResponseEntity<Map<String, Object>> triggerSignalPost() {
        communityWallPoster.publishPublicSurebetSignal();
        return ResponseEntity.ok(Map.of("status", "triggered"));
    }

    /**
     * Manually triggers an immediate freebet promo digest post to the community wall.
     */
    @PostMapping("/admin/community/promo-trigger")
    public ResponseEntity<Map<String, Object>> triggerPromoDigest() {
        communityWallPoster.publishFreebetPromoDigest();
        return ResponseEntity.ok(Map.of("status", "triggered"));
    }
}
