package pro.datawiki.igaming.vipbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.vipbot.telegram.*;

/**
 * Dispatcher that routes incoming Telegram webhook updates to the
 * appropriate handler based on update type.
 *
 * Supported update types:
 *   - message (text commands from users)
 *   - callback_query (inline keyboard interactions)
 *   - chat_join_request (user requests to join the private VIP channel)
 *   - my_chat_member (bot status changes in the channel)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateDispatcher {

    private final VipMemberService memberService;
    private final VipCommandHandler commandHandler;
    private final TelegramApiClient telegramApi;

    /**
     * Main entry point. Called by the webhook controller for each incoming update.
     *
     * @param update Deserialized Telegram update object
     */
    public void dispatch(TelegramUpdate update) {
        try {
            if (update.getChatJoinRequest() != null) {
                handleJoinRequest(update.getChatJoinRequest());
            } else if (update.getMessage() != null) {
                handleMessage(update.getMessage());
            } else if (update.getCallbackQuery() != null) {
                handleCallbackQuery(update.getCallbackQuery());
            } else if (update.getMyChatMember() != null) {
                handleMyChatMember(update.getMyChatMember());
            } else {
                log.debug("Unhandled update type for update_id={}", update.getUpdateId());
            }
        } catch (Exception e) {
            log.error("Error dispatching update_id={}: {}", update.getUpdateId(), e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // Handlers
    // -------------------------------------------------------

    private void handleJoinRequest(TelegramChatJoinRequest req) {
        TelegramUser user = req.getFrom();
        TelegramChat chat = req.getChat();
        if (user == null || chat == null) return;

        log.info("chat_join_request from userId={} ({}), chat={}", user.getId(), user.getUsername(), chat.getId());
        memberService.handleJoinRequest(user.getId(), user.getUsername(), user.getFirstName(), chat.getId());
    }

    private void handleMessage(TelegramMessage msg) {
        if (msg.getFrom() == null || Boolean.TRUE.equals(msg.getFrom().getIsBot())) return;
        if (msg.getText() == null) return;

        String text = msg.getText().trim();
        Long chatId = msg.getChat().getId();
        TelegramUser user = msg.getFrom();

        log.debug("message from userId={}, text={}", user.getId(), text);
        commandHandler.handle(chatId, user, text);
    }

    private void handleCallbackQuery(TelegramCallbackQuery cbq) {
        if (cbq.getFrom() == null) return;
        log.debug("callback_query from userId={}, data={}", cbq.getFrom().getId(), cbq.getData());
        commandHandler.handleCallback(cbq);
    }

    private void handleMyChatMember(TelegramChatMemberUpdated update) {
        // Log bot status changes (e.g., bot added/removed from channel)
        if (update.getNewChatMember() != null && update.getNewChatMember().getUser() != null) {
            String newStatus = update.getNewChatMember().getStatus();
            log.info("my_chat_member update: bot status changed to '{}' in chat={}",
                    newStatus, update.getChat() != null ? update.getChat().getId() : "unknown");
        }
    }
}
