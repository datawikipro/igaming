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
public class PlayerProfileDto {

    private Long playerId;
    private String canonicalName;
    private String slug;
    private String position;
    private String nationality;
    private Long sofascoreId;

    // Archetype Satellite
    private String archetype;
    private BigDecimal overallRating;
    private Boolean isJokerCandidate;
    private String preferredFoot;
    private BigDecimal paceRating;
    private BigDecimal shootingRating;
    private BigDecimal passingRating;
    private Integer referenceTier;
    private Object similarPlayers;

    // Active memberships across teams (e.g. Spartak + Russian National Team)
    private List<ActiveMembershipDto> memberships;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveMembershipDto {
        private Long teamId;
        private String teamName;
        private String teamSlug;
        private Boolean isNationalTeam;
        private String membershipType; // CLUB, NATIONAL_TEAM
        private Integer shirtNumber;
    }
}
