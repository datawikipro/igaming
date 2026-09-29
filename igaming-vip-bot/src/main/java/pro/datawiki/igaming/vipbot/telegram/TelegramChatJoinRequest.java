package pro.datawiki.igaming.vipbot.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Telegram ChatJoinRequest — fired when user requests to join the VIP private channel.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramChatJoinRequest {

    @JsonProperty("chat")
    private TelegramChat chat;

    @JsonProperty("from")
    private TelegramUser from;

    @JsonProperty("date")
    private Long date;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("invite_link")
    private TelegramInviteLink inviteLink;
}
