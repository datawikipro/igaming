package pro.datawiki.igaming.boosty.model;

/**
 * Boosty subscription tiers for SmartBet.guru.
 *
 * <p>Tier hierarchy (low → high):
 * <ol>
 *   <li>{@link #FREE}    — public, no payment required</li>
 *   <li>{@link #BASIC}   — entry-level paying tier, 299 ₽/month</li>
 *   <li>{@link #PREMIUM} — full-access tier, 2 500 ₽/month</li>
 * </ol>
 *
 * <p>Yields per AGENTS.md Rule #10 and social-media-bot spec:
 * <ul>
 *   <li>Free → up to 5.0% surebets shown</li>
 *   <li>Premium → unlimited (20%+ surebets access)</li>
 * </ul>
 */
public enum BoostySubscriptionTier {

    /**
     * Free public tier.
     * Access: surebets up to 5.0% yield only, basic arbitrage alerts.
     */
    FREE(
            "Free",
            0,
            "Базовый доступ к вилкам с доходностью до 5%",
            5.0
    ),

    /**
     * Basic paying tier — 299 ₽/month.
     * Access: surebets up to 10% yield, daily digest, priority Telegram alerts.
     */
    BASIC(
            "Базовый",
            299,
            "Расширенный доступ: вилки до 10%, ежедневный дайджест, Telegram-оповещения",
            10.0
    ),

    /**
     * Premium (VIP) tier — 2 500 ₽/month.
     * Access: unlimited surebets (20%+), +EV corridors, personal freebet calculator,
     * direct operator chat, affiliate payouts, Boosty-exclusive posts.
     */
    PREMIUM(
            "Premium",
            2500,
            "Полный доступ: вилки 20%+, коридоры, +EV, фрибет-калькулятор, чат с оператором",
            Double.MAX_VALUE
    );

    // ─────────────────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────────────────

    /** Human-readable display name (used in Boosty level titles). */
    private final String displayName;

    /** Monthly price in RUB (0 for FREE). */
    private final int priceRub;

    /** Short description of benefits shown in promotional materials. */
    private final String benefits;

    /**
     * Maximum surebet yield accessible to this tier (%).
     * {@link Double#MAX_VALUE} means unlimited.
     */
    private final double maxYieldPct;

    // ─────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────

    BoostySubscriptionTier(String displayName, int priceRub, String benefits, double maxYieldPct) {
        this.displayName  = displayName;
        this.priceRub     = priceRub;
        this.benefits     = benefits;
        this.maxYieldPct  = maxYieldPct;
    }

    // ─────────────────────────────────────────────────────────
    // Accessors
    // ─────────────────────────────────────────────────────────

    public String getDisplayName()  { return displayName;  }
    public int    getPriceRub()     { return priceRub;     }
    public String getBenefits()     { return benefits;     }
    public double getMaxYieldPct()  { return maxYieldPct;  }

    /**
     * Check whether the given subscription amount matches this tier.
     * Matching rule: amount >= tier price AND amount < next tier price (if any).
     *
     * @param amountRub monthly subscription amount paid (RUB)
     * @return true if this tier applies
     */
    public boolean matches(int amountRub) {
        return amountRub >= priceRub && (ordinal() == values().length - 1
                || amountRub < values()[ordinal() + 1].priceRub);
    }

    /**
     * Resolve the tier for a given subscription amount.
     *
     * @param amountRub monthly subscription amount paid (RUB)
     * @return the highest tier the amount qualifies for
     */
    public static BoostySubscriptionTier fromAmount(int amountRub) {
        BoostySubscriptionTier result = FREE;
        for (BoostySubscriptionTier tier : values()) {
            if (amountRub >= tier.priceRub) {
                result = tier;
            }
        }
        return result;
    }

    /**
     * Human-readable label for marketing copy:
     * e.g., «Premium (2 500 ₽/мес)».
     */
    public String toMarketingLabel() {
        if (priceRub == 0) {
            return displayName + " (бесплатно)";
        }
        return displayName + " (" + priceRub + " ₽/мес)";
    }
}
