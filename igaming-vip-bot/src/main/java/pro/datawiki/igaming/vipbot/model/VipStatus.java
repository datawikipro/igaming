package pro.datawiki.igaming.vipbot.model;

/**
 * VIP subscription lifecycle states.
 */
public enum VipStatus {

    /** Member has requested access but payment/portal account not verified yet. */
    PENDING,

    /** Full active VIP member with access to the private channel. */
    ACTIVE,

    /** Subscription expired or revoked; invite link invalidated. */
    REVOKED,

    /** Member was banned by admin. */
    BANNED
}
