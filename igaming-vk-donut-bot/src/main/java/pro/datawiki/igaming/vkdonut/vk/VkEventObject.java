package pro.datawiki.igaming.vkdonut.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Event-specific object payload for VK Donut subscription events.
 *
 * The structure varies by event type. For donut subscription events
 * (donut_subscription_create, donut_subscription_prolonged,
 *  donut_subscription_cancelled, donut_subscription_expired,
 *  donut_subscription_price_changed) VK populates these fields.
 *
 * Reference: https://dev.vk.com/ru/api/community-events/donut
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VkEventObject {

    /** VK user ID of the donor (present in all donut_subscription_* events). */
    @JsonProperty("user_id")
    private Long userId;

    /** Monthly donation amount in rubles. */
    @JsonProperty("amount")
    private Integer amount;

    /** Amount without VAT (for price_changed events this is the new amount). */
    @JsonProperty("amount_without_fee")
    private Integer amountWithoutFee;

    /** Previous amount (for donut_subscription_price_changed event). */
    @JsonProperty("amount_old")
    private Integer amountOld;

    /** New amount (for donut_subscription_price_changed event). */
    @JsonProperty("amount_new")
    private Integer amountNew;

    /**
     * Optional: VK user info snippet (first_name, last_name) — included in some events.
     * Resolved separately via VK API users.get when needed.
     */
    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;
}
