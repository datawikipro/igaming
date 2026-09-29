package pro.datawiki.igaming.vipbot.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Telegram ChatMemberUpdated — includes my_chat_member and chat_member events.
 * Used to detect when bot is kicked from or added to the channel.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramChatMemberUpdated {

    @JsonProperty("chat")
    private TelegramChat chat;

    @JsonProperty("from")
    private TelegramUser from;

    @JsonProperty("date")
    private Long date;

    @JsonProperty("old_chat_member")
    private TelegramChatMember oldChatMember;

    @JsonProperty("new_chat_member")
    private TelegramChatMember newChatMember;
}
