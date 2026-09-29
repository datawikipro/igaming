package pro.datawiki.igaming.vkdonut.model;

/**
 * Lifecycle status of a VK Donut donor subscription.
 */
public enum DonutStatus {

    /** Subscription is active — donor receives exclusive signals. */
    ACTIVE,

    /** Subscription was manually cancelled by the donor. */
    CANCELLED,

    /** Subscription expired (no renewal). */
    EXPIRED
}
