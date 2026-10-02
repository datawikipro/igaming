package pro.datawiki.igaming.playerfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;

import java.time.LocalDateTime;

/**
 * Data transfer object representing a player face profile with visual assets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerFaceDto {

    private Long id;
    private String playerName;
    private String normalizedName;
    private String aliases;
    private SportType sport;
    private TourType tour;
    private String country;
    private String countryCode;
    private String flagUrl;
    private Integer ranking;
    private Integer seedNumber;
    private String avatarUrl;
    private String sourceUrl;
    private AvatarSourceProvider sourceProvider;
    private Boolean webpOptimized;
    private LocalDateTime lastHarvestedAt;

    public static PlayerFaceDto fromEntity(PlayerFace entity) {
        if (entity == null) {
            return null;
        }
        return PlayerFaceDto.builder()
                .id(entity.getId())
                .playerName(entity.getPlayerName())
                .normalizedName(entity.getNormalizedName())
                .aliases(entity.getAliases())
                .sport(entity.getSport())
                .tour(entity.getTour())
                .country(entity.getCountry())
                .countryCode(entity.getCountryCode())
                .flagUrl(entity.getFlagUrl())
                .ranking(entity.getRanking())
                .seedNumber(entity.getSeedNumber())
                .avatarUrl(entity.getAvatarUrl())
                .sourceUrl(entity.getSourceUrl())
                .sourceProvider(entity.getSourceProvider())
                .webpOptimized(entity.getWebpOptimized())
                .lastHarvestedAt(entity.getLastHarvestedAt())
                .build();
    }

    public PlayerFace toEntity() {
        return PlayerFace.builder()
                .id(this.id)
                .playerName(this.playerName)
                .normalizedName(this.normalizedName != null ? this.normalizedName : PlayerFace.normalizeName(this.playerName))
                .aliases(this.aliases)
                .sport(this.sport != null ? this.sport : SportType.TENNIS)
                .tour(this.tour)
                .country(this.country)
                .countryCode(this.countryCode)
                .flagUrl(this.flagUrl)
                .ranking(this.ranking)
                .seedNumber(this.seedNumber)
                .avatarUrl(this.avatarUrl)
                .sourceUrl(this.sourceUrl)
                .sourceProvider(this.sourceProvider != null ? this.sourceProvider : AvatarSourceProvider.CUSTOM)
                .webpOptimized(this.webpOptimized != null ? this.webpOptimized : Boolean.TRUE)
                .lastHarvestedAt(this.lastHarvestedAt != null ? this.lastHarvestedAt : LocalDateTime.now())
                .build();
    }
}
