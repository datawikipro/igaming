package pro.datawiki.igaming.vkdonut.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.vkdonut.model.DonutDonor;
import pro.datawiki.igaming.vkdonut.service.DonutDonorService;
import pro.datawiki.igaming.vkdonut.service.DonutSignalBroadcaster;

import java.util.List;
import java.util.Map;

/**
 * Admin REST API for monitoring and manual operations on VK Donut donors.
 *
 * Endpoints (internal, not exposed to VK):
 *   GET  /admin/donut/donors          — list all active donors
 *   POST /admin/donut/broadcast       — send a manual broadcast message to all active donors
 */
@RestController
@RequestMapping("/admin/donut")
@RequiredArgsConstructor
@Slf4j
public class DonutAdminController {

    private final DonutDonorService donorService;
    private final DonutSignalBroadcaster broadcaster;

    /**
     * List all active Donut donors.
     */
    @GetMapping("/donors")
    public ResponseEntity<List<DonutDonor>> listActiveDonors() {
        List<DonutDonor> donors = donorService.getActiveDonors();
        return ResponseEntity.ok(donors);
    }

    /**
     * Manually trigger a broadcast message to all active Donut donors.
     *
     * Request body: {"text": "Your message here"}
     */
    @PostMapping("/broadcast")
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
    @PostMapping("/posts")
    public ResponseEntity<Map<String, Object>> publishPost(@RequestBody Map<String, Object> body) {
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
}
