package pro.datawiki.igaming.analyst.rating;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "sport_mmr_rating")
@IdClass(SportMmrRatingId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportMmrRatingEntity {

    @Id
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Id
    @Column(name = "sport_slug", length = 32, nullable = false)
    private String sportSlug;

    @Id
    @Column(name = "surface", length = 16, nullable = false)
    @Builder.Default
    private String surface = "ALL";

    @Column(name = "mmr", nullable = false)
    @Builder.Default
    private Double mmr = 1500.0;

    @Column(name = "rating_deviation", nullable = false)
    @Builder.Default
    private Double ratingDeviation = 350.0;

    @Column(name = "volatility", nullable = false)
    @Builder.Default
    private Double volatility = 0.06;

    @Column(name = "matches_played", nullable = false)
    @Builder.Default
    private Integer matchesPlayed = 0;

    @Column(name = "wins_count", nullable = false)
    @Builder.Default
    private Integer winsCount = 0;

    @Column(name = "losses_count", nullable = false)
    @Builder.Default
    private Integer lossesCount = 0;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "peak_mmr", nullable = false)
    @Builder.Default
    private Double peakMmr = 1500.0;

    @Column(name = "player_name", length = 191)
    private String playerName;

    @Column(name = "country", length = 64)
    private String country;

    @Column(name = "last_match_at")
    private Instant lastMatchAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public GlickoPlayer toGlickoPlayer() {
        return GlickoPlayer.builder()
                .playerId(this.playerId)
                .sportSlug(this.sportSlug)
                .surface(this.surface)
                .mmr(this.mmr)
                .rd(this.ratingDeviation)
                .volatility(this.volatility)
                .matchesPlayed(this.matchesPlayed)
                .wins(this.winsCount)
                .losses(this.lossesCount)
                .streak(this.currentStreak)
                .peakMmr(this.peakMmr)
                .lastMatchTime(this.lastMatchAt)
                .build();
    }

    public static SportMmrRatingEntity fromGlickoPlayer(GlickoPlayer player, String playerName, String country) {
        return SportMmrRatingEntity.builder()
                .playerId(player.getPlayerId())
                .sportSlug(player.getSportSlug())
                .surface(player.getSurface())
                .mmr(player.getMmr())
                .ratingDeviation(player.getRd())
                .volatility(player.getVolatility())
                .matchesPlayed(player.getMatchesPlayed())
                .winsCount(player.getWins())
                .lossesCount(player.getLosses())
                .currentStreak(player.getStreak())
                .peakMmr(player.getPeakMmr())
                .playerName(playerName)
                .country(country)
                .lastMatchAt(player.getLastMatchTime())
                .updatedAt(Instant.now())
                .build();
    }
}
