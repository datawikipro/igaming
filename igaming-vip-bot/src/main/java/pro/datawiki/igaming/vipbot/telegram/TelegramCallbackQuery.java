package pro.datawiki.igaming.vipbot.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Telegram CallbackQuery — from inline keyboard button presses.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramCallbackQuery {

    @JsonProperty("id")
    private String id;

    @JsonProperty("from")
    private TelegramUser from;

    @JsonProperty("message")
    private TelegramMessage message;

    @JsonProperty("data")
    private String data;
}
