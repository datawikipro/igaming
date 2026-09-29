package pro.datawiki.igaming.vkdonut.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.vkdonut.service.DonutDonorService;
import pro.datawiki.igaming.vkdonut.vk.VkCallbackEvent;
import pro.datawiki.igaming.vkdonut.vk.VkEventObject;

/**
 * VK Callback API controller.
 *
 * VK delivers all community events to POST /vk-callback.
 * Authentication is performed by checking the {@code secret} field in the JSON body
 * against the configured secret (VK_CALLBACK_SECRET).
 *
 * VK Donut event types handled:
 *   - confirmation                       → server URL verification
 *   - donut_subscription_create          → new donor subscribes
 *   - donut_subscription_prolonged       → donor renews subscription
 *   - donut_subscription_cancelled       → donor cancels
 *   - donut_subscription_expired         → subscription expires without renewal
 *   - donut_subscription_price_changed   → donor changes their amount
 *
 * Reference: https://dev.vk.com/ru/api/community-events/donut
 */
@RestController
@RequestMapping("/vk-callback")
@RequiredArgsConstructor
@Slf4j
public class VkCallbackController {

    /** VK event type constants */
    private static final String TYPE_CONFIRMATION             = "confirmation";
    private static final String TYPE_DONUT_CREATE             = "donut_subscription_create";
    private static final String TYPE_DONUT_PROLONGED          = "donut_subscription_prolonged";
    private static final String TYPE_DONUT_CANCELLED          = "donut_subscription_cancelled";
    private static final String TYPE_DONUT_EXPIRED            = "donut_subscription_expired";
    private static final String TYPE_DONUT_PRICE_CHANGED      = "donut_subscription_price_changed";

    private final DonutDonorService donorService;

    @Value("${vk.callback.secret:changeme}")
    private String callbackSecret;

    @Value("${vk.callback.confirmation:REPLACE}")
    private String confirmationString;

    @Value("${vk.community.id:0}")
    private Long communityId;

    // -------------------------------------------------------
    // Main callback endpoint
    // -------------------------------------------------------

    /**
     * Receive a VK Callback API event.
     *
     * VK expects either:
     *   - "ok" on successful processing
     *   - The confirmation string for server verification events
     *
     * @param event Deserialized VK Callback event
     */
    @PostMapping
    public ResponseEntity<String> handleCallback(@RequestBody VkCallbackEvent event) {
        // ── 1. Verify secret ──
        if (!callbackSecret.equals(event.getSecret())) {
            log.warn("VK Callback: rejected request with invalid secret (type={})", event.getType());
            return ResponseEntity.status(403).body("forbidden");
        }

        // ── 2. Verify community ID ──
        if (communityId > 0 && !communityId.equals(event.getGroupId())) {
            log.warn("VK Callback: group_id mismatch: expected={}, got={}", communityId, event.getGroupId());
            return ResponseEntity.badRequest().body("bad_group");
        }

        String type = event.getType();
        log.debug("VK Callback event received: type={}, group_id={}", type, event.getGroupId());

        // ── 3. Dispatch by type ──
        try {
            switch (type) {
                case TYPE_CONFIRMATION -> {
                    log.info("VK Callback: server confirmation requested, responding with confirmation string.");
                    return ResponseEntity.ok(confirmationString);
                }
                case TYPE_DONUT_CREATE -> handleDonutCreate(event.getObject());
                case TYPE_DONUT_PROLONGED -> handleDonutProlonged(event.getObject());
                case TYPE_DONUT_CANCELLED -> handleDonutCancelled(event.getObject());
                case TYPE_DONUT_EXPIRED -> handleDonutExpired(event.getObject());
                case TYPE_DONUT_PRICE_CHANGED -> handleDonutPriceChanged(event.getObject());
                default -> log.debug("VK Callback: unhandled event type: {}", type);
            }
        } catch (Exception e) {
            log.error("VK Callback: error processing type={}: {}", type, e.getMessage(), e);
            // Return "ok" anyway — VK retries on non-200 responses causing duplicate events
        }

        // VK expects exactly "ok" (plain text, 200) to acknowledge the event
        return ResponseEntity.ok("ok");
    }

    // -------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------

    private void handleDonutCreate(VkEventObject obj) {
        if (obj == null || obj.getUserId() == null) {
            log.warn("donut_subscription_create: missing object or user_id");
            return;
        }
        log.info("donut_subscription_create: vkUserId={}, amount={}", obj.getUserId(), obj.getAmount());
        donorService.onSubscriptionCreate(obj.getUserId(), obj.getAmount());
    }

    private void handleDonutProlonged(VkEventObject obj) {
        if (obj == null || obj.getUserId() == null) {
            log.warn("donut_subscription_prolonged: missing object or user_id");
            return;
        }
        log.info("donut_subscription_prolonged: vkUserId={}, amount={}", obj.getUserId(), obj.getAmount());
        donorService.onSubscriptionProlonged(obj.getUserId(), obj.getAmount());
    }

    private void handleDonutCancelled(VkEventObject obj) {
        if (obj == null || obj.getUserId() == null) {
            log.warn("donut_subscription_cancelled: missing object or user_id");
            return;
        }
        log.info("donut_subscription_cancelled: vkUserId={}", obj.getUserId());
        donorService.onSubscriptionCancelled(obj.getUserId());
    }

    private void handleDonutExpired(VkEventObject obj) {
        if (obj == null || obj.getUserId() == null) {
            log.warn("donut_subscription_expired: missing object or user_id");
            return;
        }
        log.info("donut_subscription_expired: vkUserId={}", obj.getUserId());
        donorService.onSubscriptionExpired(obj.getUserId());
    }

    private void handleDonutPriceChanged(VkEventObject obj) {
        if (obj == null || obj.getUserId() == null) {
            log.warn("donut_subscription_price_changed: missing object or user_id");
            return;
        }
        Integer newAmount = obj.getAmountNew() != null ? obj.getAmountNew() : obj.getAmount();
        log.info("donut_subscription_price_changed: vkUserId={}, amountNew={}", obj.getUserId(), newAmount);
        donorService.onSubscriptionPriceChanged(obj.getUserId(), newAmount);
    }
}
