package pro.datawiki.igaming.vipbot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.vipbot.model.VipMember;
import pro.datawiki.igaming.vipbot.service.VipMemberService;
import pro.datawiki.igaming.vipbot.service.VipSignalBroadcaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal admin REST API for the VIP bot.
 * Used by platform ops and the portal to manage VIP members programmatically.
 *
 * All endpoints are internal-only (not exposed externally via Ingress).
 */
@RestController
@RequestMapping("/api/v1/vip")
@RequiredArgsConstructor
public class VipAdminController {

    private final VipMemberService memberService;
    private final VipSignalBroadcaster broadcaster;

    // -------------------------------------------------------
    // Member Management
    // -------------------------------------------------------

    /**
     * GET /api/v1/vip/members — List all VIP members.
     */
    @GetMapping("/members")
    public ResponseEntity<List<VipMember>> getAllMembers() {
        return ResponseEntity.ok(memberService.getActiveMembers());
    }

    /**
     * POST /api/v1/vip/members/{telegramUserId}/activate — Activate a VIP member.
     */
    @PostMapping("/members/{telegramUserId}/activate")
    public ResponseEntity<Map<String, Object>> activateMember(
            @PathVariable Long telegramUserId) {

        boolean ok = memberService.activateMember(telegramUserId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", ok);
        resp.put("telegramUserId", telegramUserId);
        resp.put("action", "ACTIVATE");
        return ok ? ResponseEntity.ok(resp) : ResponseEntity.notFound().build();
    }

    /**
     * POST /api/v1/vip/members/{telegramUserId}/revoke — Revoke VIP access.
     */
    @PostMapping("/members/{telegramUserId}/revoke")
    public ResponseEntity<Map<String, Object>> revokeMember(
            @PathVariable Long telegramUserId) {

        memberService.revokeMember(telegramUserId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("telegramUserId", telegramUserId);
        resp.put("action", "REVOKE");
        return ResponseEntity.ok(resp);
    }

    // -------------------------------------------------------
    // Broadcast
    // -------------------------------------------------------

    /**
     * POST /api/v1/vip/broadcast — Admin broadcast message to all ACTIVE members.
     * Body: { "text": "..." }
     */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> broadcastMessage(
            @RequestBody Map<String, String> body) {

        String text = body.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "text is required"));
        }

        int activeCount = memberService.getActiveMembers().size();
        broadcaster.broadcastAdminMessage(text);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "recipients", activeCount
        ));
    }

    /**
     * POST /api/v1/vip/publish-post — Publish an exclusive post to the VIP channel.
     * Body: { "title": "...", "content": "...", "protect_content": true }
     */
    @PostMapping("/publish-post")
    public ResponseEntity<Map<String, Object>> publishPost(
            @RequestBody Map<String, Object> body) {

        String title = body.get("title") != null ? body.get("title").toString() : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "content is required"));
        }

        boolean protectContent = true;
        if (body.containsKey("protect_content")) {
            protectContent = Boolean.parseBoolean(body.get("protect_content").toString());
        }

        String message = broadcaster.publishExclusivePost(title, content, protectContent);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("protect_content", protectContent);
        resp.put("message_length", message.length());
        resp.put("channel_id", broadcaster.getVipChannelId());
        return ResponseEntity.ok(resp);
    }

    // -------------------------------------------------------
    // Status
    // -------------------------------------------------------

    /**
     * GET /api/v1/vip/status — Returns VIP community stats.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        int active  = memberService.getActiveMembers().size();
        int pending = memberService.getPendingMembers().size();
        Map<String, Object> resp = new HashMap<>();
        resp.put("service", "igaming-vip-bot");
        resp.put("bot_username", "@SmartBetVipBot");
        resp.put("active_members", active);
        resp.put("pending_members", pending);
        resp.put("status", "RUNNING");
        return ResponseEntity.ok(resp);
    }
}
