package pro.datawiki.igaming.boosty.model;

/**
 * Boosty donor/subscriber status lifecycle.
 *
 * <p>Transitions:
 * <pre>
 *   ACTIVE ──cancel──→ CANCELLED
 *   ACTIVE ──expire──→ EXPIRED
 *   CANCELLED / EXPIRED ──re-subscribe──→ ACTIVE
 * </pre>
 */
public enum BoostyDonorStatus {
    /** Active paid subscriber (currently donating). */
    ACTIVE,
    /** Subscription cancelled by the user before expiry. */
    CANCELLED,
    /** Subscription expired (not renewed). */
    EXPIRED
}
