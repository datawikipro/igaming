package pro.datawiki.igaming.analyst.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Universal Glicko-2 Competitor Model.
 * Tracks continuous rating (mu/MMR), uncertainty (phi/RD), and form volatility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlickoPlayer {

    public static final double GLICKO2_SCALE = 173.7178;
    public static final double DEFAULT_MMR = 1500.0;
    public static final double DEFAULT_RD = 350.0;
    public static final double DEFAULT_VOLATILITY = 0.06;

    private long playerId;
    private String sportSlug;
    private String surface; // "ALL", "HARD", "CLAY", "GRASS"

    @Builder.Default
    private double mmr = DEFAULT_MMR;

    @Builder.Default
    private double rd = DEFAULT_RD;

    @Builder.Default
    private double volatility = DEFAULT_VOLATILITY;

    @Builder.Default
    private int matchesPlayed = 0;

    @Builder.Default
    private int wins = 0;

    @Builder.Default
    private int losses = 0;

    @Builder.Default
    private int streak = 0;

    @Builder.Default
    private double peakMmr = DEFAULT_MMR;

    private Instant lastMatchTime;

    public double getMu() {
        return (this.mmr - DEFAULT_MMR) / GLICKO2_SCALE;
    }

    public double getPhi() {
        return this.rd / GLICKO2_SCALE;
    }

    public void setGlicko2(double mu, double phi, double vol) {
        this.mmr = (mu * GLICKO2_SCALE) + DEFAULT_MMR;
        this.rd = Math.min(350.0, Math.max(30.0, phi * GLICKO2_SCALE));
        this.volatility = vol;
        if (this.mmr > this.peakMmr) {
            this.peakMmr = this.mmr;
        }
    }

    public static GlickoPlayer ofDefault(long playerId, String sportSlug, String surface) {
        return GlickoPlayer.builder()
                .playerId(playerId)
                .sportSlug(sportSlug != null ? sportSlug : "tennis")
                .surface(surface != null ? surface : "ALL")
                .mmr(DEFAULT_MMR)
                .rd(DEFAULT_RD)
                .volatility(DEFAULT_VOLATILITY)
                .peakMmr(DEFAULT_MMR)
                .build();
    }
}
