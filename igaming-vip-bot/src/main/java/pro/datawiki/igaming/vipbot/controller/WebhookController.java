package pro.datawiki.igaming.vipbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.vipbot.service.UpdateDispatcher;
import pro.datawiki.igaming.vipbot.telegram.TelegramUpdate;

import java.util.Map;

/**
 * Telegram Webhook controller.
 *
 * Telegram delivers updates to POST /webhook/{secret}.
 * The secret token path segment acts as a lightweight auth mechanism.
 *
 * Endpoint: POST /webhook/{secret}
 */
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final UpdateDispatcher dispatcher;

    @Value("${vip.bot.webhook-secret:changeme}")
    private String webhookSecret;

    /**
     * Receive a Telegram update.
     *
     * @param secret   Path variable serving as webhook auth token
     * @param update   Deserialized Telegram update
     */
    @PostMapping("/{secret}")
    public ResponseEntity<Map<String, String>> receiveUpdate(
            @PathVariable String secret,
            @RequestBody TelegramUpdate update) {

        if (!webhookSecret.equals(secret)) {
            log.warn("Webhook: rejected request with invalid secret");
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        log.debug("Webhook: received update_id={}", update.getUpdateId());
        dispatcher.dispatch(update);

        // Telegram expects a 2xx response quickly; processing is synchronous but fast
        return ResponseEntity.ok(Map.of("ok", "true"));
    }
}
