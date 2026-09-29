package pro.datawiki.igaming.boosty.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Sends notifications to a Telegram chat (donor admin chat) via Telegram Bot API.
 *
 * <p>Used for:
 * <ul>
 *   <li>Welcome/farewell messages when donors subscribe/unsubscribe on Boosty</li>
 *   <li>Forwarded donor comments from Boosty posts</li>
 * </ul>
 *
 * <p>If {@code BOOSTY_TELEGRAM_BOT_TOKEN} or {@code BOOSTY_TELEGRAM_DONOR_CHAT_ID}
 * are not configured, notifications are silently skipped (log WARN only).
 * This allows the service to run without Telegram configured (e.g., in dev).
 */
@Service
@Slf4j
public class BoostyTelegramNotifier {

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";
    private static final int REQUEST_TIMEOUT_SEC = 10;

    @Value("${boosty.telegram.bot-token:}")
    private String botToken;

    @Value("${boosty.telegram.donor-chat-id:}")
    private String donorChatId;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(REQUEST_TIMEOUT_SEC))
            .build();

    /**
     * Send a message to the configured Telegram donor chat.
     *
     * <p>Silently skips if token or chat ID is not configured.
     *
     * @param text message text (Markdown optional, sent as plain text)
     */
    public void sendToDonorChat(String text) {
        if (botToken == null || botToken.isBlank()) {
            log.debug("BoostyTelegramNotifier: bot token not configured, skipping notification");
            return;
        }
        if (donorChatId == null || donorChatId.isBlank()) {
            log.debug("BoostyTelegramNotifier: donor chat ID not configured, skipping notification");
            return;
        }

        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = String.format("%s%s/sendMessage?chat_id=%s&text=%s",
                    TELEGRAM_API_BASE, botToken, donorChatId, encodedText);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SEC))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("BoostyTelegramNotifier: Telegram API returned HTTP {}: {}",
                        response.statusCode(), response.body());
            } else {
                log.debug("BoostyTelegramNotifier: message sent to donor chat");
            }
        } catch (IOException | InterruptedException e) {
            log.warn("BoostyTelegramNotifier: failed to send message: {}", e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
