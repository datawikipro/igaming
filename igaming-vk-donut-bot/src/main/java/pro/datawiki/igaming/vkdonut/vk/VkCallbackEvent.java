package pro.datawiki.igaming.vkdonut.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Root VK Callback API event payload.
 *
 * VK sends a POST with this structure to the registered callback URL.
 * Reference: https://dev.vk.com/ru/api/callback/getting-started
 *
 * Example:
 * <pre>
 * {
 *   "type": "donut_subscription_create",
 *   "group_id": 123456789,
 *   "secret": "my_secret",
 *   "object": { ... }
 * }
 * </pre>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VkCallbackEvent {

    /** Event type identifier (e.g. "confirmation", "donut_subscription_create"). */
    @JsonProperty("type")
    private String type;

    /** The VK community (group) ID that sent this event. */
    @JsonProperty("group_id")
    private Long groupId;

    /** Secret string for request authentication (must match configured secret). */
    @JsonProperty("secret")
    private String secret;

    /**
     * Event-specific payload.
     * For donut_subscription_* events contains donor info (user_id, amount, etc.)
     */
    @JsonProperty("object")
    private VkEventObject object;
}
