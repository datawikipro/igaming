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
}
