package pro.datawiki.igaming.vipbot.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Telegram User object (partial).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUser {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("username")
    private String username;

    @JsonProperty("is_bot")
    private Boolean isBot;
}
