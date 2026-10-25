package pro.datawiki.igaming.mdm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamProfileDto {

    private Long teamId;
    private Integer sportId;
    private String sportSlug;
    private String slug;
    private String canonicalName;
    private String logoUrl;
    private String gender;
    private String ageCategory;
    private Boolean isNational;
    private Boolean isIndividual;
    private Long sofascoreId;

    // Satellite: DNA
    private TeamDnaDto dna;

    // Satellite: SofaScore Metadata
    private TeamSofaMetadataDto sofaMetadata;

    // Satellite: Squad & Players
    private List<SquadMemberDto> squad;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamDnaDto {
        private String primaryFormation;
        private String playStyle;
        private BigDecimal pressingIntensity;
        private BigDecimal possessionAvgPct;
        private BigDecimal aerialThreatScore;
        private BigDecimal defenseVulnerabilityScore;
        private Integer referenceTier;
        private Object similarTeams;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamSofaMetadataDto {
        private String venueName;
        private String venueCity;
        private Integer venueCapacity;
        private String coachName;
        private BigDecimal sofascoreRating;
        private String formRecent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SquadMemberDto {
        private Long playerId;
        private String playerName;
        private String playerSlug;
        private String position;
        private Integer shirtNumber;
        private String membershipType; // CLUB, NATIONAL_TEAM
        private Boolean isJokerCandidate;
        private BigDecimal overallRating;
        private String archetype;
        private Object similarPlayers;
    }
}
