package pro.datawiki.igaming.vipbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client wrapper around the Telegram Bot API.
 *
 * All calls use the standard HTTPS Bot API endpoint:
 *   https://api.telegram.org/bot{TOKEN}/{method}
 *
 * Responses are parsed minimally — we log on failure and continue (fail-soft).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramApiClient {

    private final RestTemplate restTemplate;

    @Value("${vip.bot.token}")
    private String botToken;

    private static final String API_BASE = "https://api.telegram.org/bot";

    // -------------------------------------------------------
    // Public API Methods
    // -------------------------------------------------------

    /**
     * Send a plain text message to a chat.
     *
     * @param chatId  Telegram chat ID (can be user chat or channel ID)
     * @param text    Message text (supports HTML parse mode)
     */
    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, false);
    }

    /**
     * Send a plain text message to a chat with optional content protection.
     *
     * @param chatId          Telegram chat ID (can be user chat or channel ID)
     * @param text            Message text (supports HTML parse mode)
     * @param protectContent  If true, Telegram forbids forwarding and screenshots
     */
    public void sendMessage(Long chatId, String text, boolean protectContent) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        body.put("parse_mode", "HTML");
        if (protectContent) {
            body.put("protect_content", true);
        }
        post("sendMessage", body);
    }

    /**
     * Send a message with an inline keyboard.
     *
     * @param chatId      Telegram chat ID
     * @param text        Message text
     * @param replyMarkup Telegram InlineKeyboardMarkup as a Map
     */
    public void sendMessageWithKeyboard(Long chatId, String text, Map<String, Object> replyMarkup) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        body.put("parse_mode", "HTML");
        body.put("reply_markup", replyMarkup);
        post("sendMessage", body);
    }

    /**
     * Answer a callback query (dismiss the loading spinner on inline buttons).
     *
     * @param callbackQueryId Telegram callback query ID
     * @param text            Optional toast notification text
     */
    public void answerCallbackQuery(String callbackQueryId, String text) {
        Map<String, Object> body = new HashMap<>();
        body.put("callback_query_id", callbackQueryId);
        if (text != null && !text.isBlank()) {
            body.put("text", text);
        }
        post("answerCallbackQuery", body);
    }

    /**
     * Approve a pending chat join request (grants the user access to the private channel).
     *
     * @param chatId         Channel/group chat ID
     * @param telegramUserId User ID to approve
     */
    public void approveChatJoinRequest(Long chatId, Long telegramUserId) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("user_id", telegramUserId);
        post("approveChatJoinRequest", body);
    }

    /**
     * Decline a pending chat join request.
     *
     * @param chatId         Channel/group chat ID
     * @param telegramUserId User ID to decline
     */
    public void declineChatJoinRequest(Long chatId, Long telegramUserId) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("user_id", telegramUserId);
        post("declineChatJoinRequest", body);
    }

    /**
     * Ban (kick) a user from the VIP channel.
     *
     * @param chatId         Channel chat ID
     * @param telegramUserId User to ban
     */
    public void banChatMember(Long chatId, Long telegramUserId) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("user_id", telegramUserId);
        post("banChatMember", body);
    }

    /**
     * Unban a previously banned member (required before re-inviting).
     *
     * @param chatId         Channel chat ID
     * @param telegramUserId User to unban
     * @param onlyIfBanned   Only unban if the user is actually banned
     */
    public void unbanChatMember(Long chatId, Long telegramUserId, boolean onlyIfBanned) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("user_id", telegramUserId);
        body.put("only_if_banned", onlyIfBanned);
        post("unbanChatMember", body);
    }

    /**
     * Create a new one-time invite link for the VIP channel (creates_join_request = true
     * so bot must approve each request individually).
     *
     * @param chatId     Channel chat ID
     * @param name       Human-readable link name
     * @param memberLimit 0 = unlimited
     * @return The created invite link string, or null on failure
     */
    public String createChatInviteLink(Long chatId, String name, int memberLimit) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("name", name);
        body.put("creates_join_request", true);
        if (memberLimit > 0) {
            body.put("member_limit", memberLimit);
        }
        Map<String, Object> resp = post("createChatInviteLink", body);
        if (resp != null && Boolean.TRUE.equals(resp.get("ok"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) resp.get("result");
            if (result != null) {
                return (String) result.get("invite_link");
            }
        }
        return null;
    }

    /**
     * Register a webhook URL for the bot.
     *
     * @param webhookUrl Full HTTPS URL
     * @param secretToken Optional secret token header value
     */
    public void setWebhook(String webhookUrl, String secretToken) {
        Map<String, Object> body = new HashMap<>();
        body.put("url", webhookUrl);
        body.put("allowed_updates", new String[]{
                "message", "callback_query", "chat_join_request", "my_chat_member"
        });
        if (secretToken != null && !secretToken.isBlank()) {
            body.put("secret_token", secretToken);
        }
        Map<String, Object> resp = post("setWebhook", body);
        log.info("setWebhook response: {}", resp);
    }

    // -------------------------------------------------------
    // Internal HTTP helper
    // -------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String method, Map<String, Object> body) {
        String url = API_BASE + botToken + "/" + method;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Telegram API [{}] returned HTTP {}: {}", method, response.getStatusCode(), response.getBody());
            }
            return (Map<String, Object>) response.getBody();
        } catch (Exception e) {
            log.error("Telegram API [{}] call failed: {}", method, e.getMessage());
            return null;
        }
    }
}
