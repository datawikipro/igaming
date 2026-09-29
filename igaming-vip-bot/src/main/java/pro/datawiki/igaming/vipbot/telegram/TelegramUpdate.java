package pro.datawiki.igaming.vipbot.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Telegram Bot API update object (partial — only fields we process).
 * See: https://core.telegram.org/bots/api#update
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {

    @JsonProperty("update_id")
    private Long updateId;

    @JsonProperty("message")
    private TelegramMessage message;

    @JsonProperty("callback_query")
    private TelegramCallbackQuery callbackQuery;

    @JsonProperty("chat_join_request")
    private TelegramChatJoinRequest chatJoinRequest;

    @JsonProperty("my_chat_member")
    private TelegramChatMemberUpdated myChatMember;
}
