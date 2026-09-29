package pro.datawiki.igaming.vipbot.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Telegram InviteLink object (partial).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramInviteLink {

    @JsonProperty("invite_link")
    private String inviteLink;

    @JsonProperty("creator")
    private TelegramUser creator;

    @JsonProperty("creates_join_request")
    private Boolean createsJoinRequest;

    @JsonProperty("is_primary")
    private Boolean isPrimary;

    @JsonProperty("is_revoked")
    private Boolean isRevoked;
}
