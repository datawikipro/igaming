package pro.datawiki.igaming.vipbot.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Telegram ChatMember — single member status snapshot.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramChatMember {

    @JsonProperty("user")
    private TelegramUser user;

    /** Status: creator, administrator, member, restricted, left, kicked */
    @JsonProperty("status")
    private String status;
}
