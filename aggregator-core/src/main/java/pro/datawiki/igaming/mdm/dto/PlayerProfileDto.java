/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pro.datawiki.igaming.mdm.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Generated;

public class PlayerProfileDto {
    private Long playerId;
    private String canonicalName;
    private String slug;
    private String position;
    private String nationality;
    private Long sofascoreId;
    private String archetype;
    private BigDecimal overallRating;
    private Boolean isJokerCandidate;
    private String preferredFoot;
    private BigDecimal paceRating;
    private BigDecimal shootingRating;
    private BigDecimal passingRating;
    private Integer referenceTier;
    private Object similarPlayers;
    private List<ActiveMembershipDto> memberships;

    @Generated
    public static PlayerProfileDtoBuilder builder() {
        return new PlayerProfileDtoBuilder();
    }

    @Generated
    public Long getPlayerId() {
        return this.playerId;
    }

    @Generated
    public String getCanonicalName() {
        return this.canonicalName;
    }

    @Generated
    public String getSlug() {
        return this.slug;
    }

    @Generated
    public String getPosition() {
        return this.position;
    }

    @Generated
    public String getNationality() {
        return this.nationality;
    }

    @Generated
    public Long getSofascoreId() {
        return this.sofascoreId;
    }

    @Generated
    public String getArchetype() {
        return this.archetype;
    }

    @Generated
    public BigDecimal getOverallRating() {
        return this.overallRating;
    }

    @Generated
    public Boolean getIsJokerCandidate() {
        return this.isJokerCandidate;
    }

    @Generated
    public String getPreferredFoot() {
        return this.preferredFoot;
    }

    @Generated
    public BigDecimal getPaceRating() {
        return this.paceRating;
    }

    @Generated
    public BigDecimal getShootingRating() {
        return this.shootingRating;
    }

    @Generated
    public BigDecimal getPassingRating() {
        return this.passingRating;
    }

    @Generated
    public Integer getReferenceTier() {
        return this.referenceTier;
    }

    @Generated
    public Object getSimilarPlayers() {
        return this.similarPlayers;
    }

    @Generated
    public List<ActiveMembershipDto> getMemberships() {
        return this.memberships;
    }

    @Generated
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    @Generated
    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    @Generated
    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Generated
    public void setPosition(String position) {
        this.position = position;
    }

    @Generated
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    @Generated
    public void setSofascoreId(Long sofascoreId) {
        this.sofascoreId = sofascoreId;
    }

    @Generated
    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    @Generated
    public void setOverallRating(BigDecimal overallRating) {
        this.overallRating = overallRating;
    }

    @Generated
    public void setIsJokerCandidate(Boolean isJokerCandidate) {
        this.isJokerCandidate = isJokerCandidate;
    }

    @Generated
    public void setPreferredFoot(String preferredFoot) {
        this.preferredFoot = preferredFoot;
    }

    @Generated
    public void setPaceRating(BigDecimal paceRating) {
        this.paceRating = paceRating;
    }

    @Generated
    public void setShootingRating(BigDecimal shootingRating) {
        this.shootingRating = shootingRating;
    }

    @Generated
    public void setPassingRating(BigDecimal passingRating) {
        this.passingRating = passingRating;
    }

    @Generated
    public void setReferenceTier(Integer referenceTier) {
        this.referenceTier = referenceTier;
    }

    @Generated
    public void setSimilarPlayers(Object similarPlayers) {
        this.similarPlayers = similarPlayers;
    }

    @Generated
    public void setMemberships(List<ActiveMembershipDto> memberships) {
        this.memberships = memberships;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PlayerProfileDto)) {
            return false;
        }
        PlayerProfileDto other = (PlayerProfileDto)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$playerId = this.getPlayerId();
        Long other$playerId = other.getPlayerId();
        if (this$playerId == null ? other$playerId != null : !((Object)this$playerId).equals(other$playerId)) {
            return false;
        }
        Long this$sofascoreId = this.getSofascoreId();
        Long other$sofascoreId = other.getSofascoreId();
        if (this$sofascoreId == null ? other$sofascoreId != null : !((Object)this$sofascoreId).equals(other$sofascoreId)) {
            return false;
        }
        Boolean this$isJokerCandidate = this.getIsJokerCandidate();
        Boolean other$isJokerCandidate = other.getIsJokerCandidate();
        if (this$isJokerCandidate == null ? other$isJokerCandidate != null : !((Object)this$isJokerCandidate).equals(other$isJokerCandidate)) {
            return false;
        }
        Integer this$referenceTier = this.getReferenceTier();
        Integer other$referenceTier = other.getReferenceTier();
        if (this$referenceTier == null ? other$referenceTier != null : !((Object)this$referenceTier).equals(other$referenceTier)) {
            return false;
        }
        String this$canonicalName = this.getCanonicalName();
        String other$canonicalName = other.getCanonicalName();
        if (this$canonicalName == null ? other$canonicalName != null : !this$canonicalName.equals(other$canonicalName)) {
            return false;
        }
        String this$slug = this.getSlug();
        String other$slug = other.getSlug();
        if (this$slug == null ? other$slug != null : !this$slug.equals(other$slug)) {
            return false;
        }
        String this$position = this.getPosition();
        String other$position = other.getPosition();
        if (this$position == null ? other$position != null : !this$position.equals(other$position)) {
            return false;
        }
        String this$nationality = this.getNationality();
        String other$nationality = other.getNationality();
        if (this$nationality == null ? other$nationality != null : !this$nationality.equals(other$nationality)) {
            return false;
        }
        String this$archetype = this.getArchetype();
        String other$archetype = other.getArchetype();
        if (this$archetype == null ? other$archetype != null : !this$archetype.equals(other$archetype)) {
            return false;
        }
        BigDecimal this$overallRating = this.getOverallRating();
        BigDecimal other$overallRating = other.getOverallRating();
        if (this$overallRating == null ? other$overallRating != null : !((Object)this$overallRating).equals(other$overallRating)) {
            return false;
        }
        String this$preferredFoot = this.getPreferredFoot();
        String other$preferredFoot = other.getPreferredFoot();
        if (this$preferredFoot == null ? other$preferredFoot != null : !this$preferredFoot.equals(other$preferredFoot)) {
            return false;
        }
        BigDecimal this$paceRating = this.getPaceRating();
        BigDecimal other$paceRating = other.getPaceRating();
        if (this$paceRating == null ? other$paceRating != null : !((Object)this$paceRating).equals(other$paceRating)) {
            return false;
        }
        BigDecimal this$shootingRating = this.getShootingRating();
        BigDecimal other$shootingRating = other.getShootingRating();
        if (this$shootingRating == null ? other$shootingRating != null : !((Object)this$shootingRating).equals(other$shootingRating)) {
            return false;
        }
        BigDecimal this$passingRating = this.getPassingRating();
        BigDecimal other$passingRating = other.getPassingRating();
        if (this$passingRating == null ? other$passingRating != null : !((Object)this$passingRating).equals(other$passingRating)) {
            return false;
        }
        Object this$similarPlayers = this.getSimilarPlayers();
        Object other$similarPlayers = other.getSimilarPlayers();
        if (this$similarPlayers == null ? other$similarPlayers != null : !this$similarPlayers.equals(other$similarPlayers)) {
            return false;
        }
        List<ActiveMembershipDto> this$memberships = this.getMemberships();
        List<ActiveMembershipDto> other$memberships = other.getMemberships();
        return !(this$memberships == null ? other$memberships != null : !((Object)this$memberships).equals(other$memberships));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof PlayerProfileDto;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $playerId = this.getPlayerId();
        result = result * 59 + ($playerId == null ? 43 : ((Object)$playerId).hashCode());
        Long $sofascoreId = this.getSofascoreId();
        result = result * 59 + ($sofascoreId == null ? 43 : ((Object)$sofascoreId).hashCode());
        Boolean $isJokerCandidate = this.getIsJokerCandidate();
        result = result * 59 + ($isJokerCandidate == null ? 43 : ((Object)$isJokerCandidate).hashCode());
        Integer $referenceTier = this.getReferenceTier();
        result = result * 59 + ($referenceTier == null ? 43 : ((Object)$referenceTier).hashCode());
        String $canonicalName = this.getCanonicalName();
        result = result * 59 + ($canonicalName == null ? 43 : $canonicalName.hashCode());
        String $slug = this.getSlug();
        result = result * 59 + ($slug == null ? 43 : $slug.hashCode());
        String $position = this.getPosition();
        result = result * 59 + ($position == null ? 43 : $position.hashCode());
        String $nationality = this.getNationality();
        result = result * 59 + ($nationality == null ? 43 : $nationality.hashCode());
        String $archetype = this.getArchetype();
        result = result * 59 + ($archetype == null ? 43 : $archetype.hashCode());
        BigDecimal $overallRating = this.getOverallRating();
        result = result * 59 + ($overallRating == null ? 43 : ((Object)$overallRating).hashCode());
        String $preferredFoot = this.getPreferredFoot();
        result = result * 59 + ($preferredFoot == null ? 43 : $preferredFoot.hashCode());
        BigDecimal $paceRating = this.getPaceRating();
        result = result * 59 + ($paceRating == null ? 43 : ((Object)$paceRating).hashCode());
        BigDecimal $shootingRating = this.getShootingRating();
        result = result * 59 + ($shootingRating == null ? 43 : ((Object)$shootingRating).hashCode());
        BigDecimal $passingRating = this.getPassingRating();
        result = result * 59 + ($passingRating == null ? 43 : ((Object)$passingRating).hashCode());
        Object $similarPlayers = this.getSimilarPlayers();
        result = result * 59 + ($similarPlayers == null ? 43 : $similarPlayers.hashCode());
        List<ActiveMembershipDto> $memberships = this.getMemberships();
        result = result * 59 + ($memberships == null ? 43 : ((Object)$memberships).hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "PlayerProfileDto(playerId=" + this.getPlayerId() + ", canonicalName=" + this.getCanonicalName() + ", slug=" + this.getSlug() + ", position=" + this.getPosition() + ", nationality=" + this.getNationality() + ", sofascoreId=" + this.getSofascoreId() + ", archetype=" + this.getArchetype() + ", overallRating=" + String.valueOf(this.getOverallRating()) + ", isJokerCandidate=" + this.getIsJokerCandidate() + ", preferredFoot=" + this.getPreferredFoot() + ", paceRating=" + String.valueOf(this.getPaceRating()) + ", shootingRating=" + String.valueOf(this.getShootingRating()) + ", passingRating=" + String.valueOf(this.getPassingRating()) + ", referenceTier=" + this.getReferenceTier() + ", similarPlayers=" + String.valueOf(this.getSimilarPlayers()) + ", memberships=" + String.valueOf(this.getMemberships()) + ")";
    }

    @Generated
    public PlayerProfileDto() {
    }

    @Generated
    public PlayerProfileDto(Long playerId, String canonicalName, String slug, String position, String nationality, Long sofascoreId, String archetype, BigDecimal overallRating, Boolean isJokerCandidate, String preferredFoot, BigDecimal paceRating, BigDecimal shootingRating, BigDecimal passingRating, Integer referenceTier, Object similarPlayers, List<ActiveMembershipDto> memberships) {
        this.playerId = playerId;
        this.canonicalName = canonicalName;
        this.slug = slug;
        this.position = position;
        this.nationality = nationality;
        this.sofascoreId = sofascoreId;
        this.archetype = archetype;
        this.overallRating = overallRating;
        this.isJokerCandidate = isJokerCandidate;
        this.preferredFoot = preferredFoot;
        this.paceRating = paceRating;
        this.shootingRating = shootingRating;
        this.passingRating = passingRating;
        this.referenceTier = referenceTier;
        this.similarPlayers = similarPlayers;
        this.memberships = memberships;
    }

    @Generated
    public static class PlayerProfileDtoBuilder {
        @Generated
        private Long playerId;
        @Generated
        private String canonicalName;
        @Generated
        private String slug;
        @Generated
        private String position;
        @Generated
        private String nationality;
        @Generated
        private Long sofascoreId;
        @Generated
        private String archetype;
        @Generated
        private BigDecimal overallRating;
        @Generated
        private Boolean isJokerCandidate;
        @Generated
        private String preferredFoot;
        @Generated
        private BigDecimal paceRating;
        @Generated
        private BigDecimal shootingRating;
        @Generated
        private BigDecimal passingRating;
        @Generated
        private Integer referenceTier;
        @Generated
        private Object similarPlayers;
        @Generated
        private List<ActiveMembershipDto> memberships;

        @Generated
        PlayerProfileDtoBuilder() {
        }

        @Generated
        public PlayerProfileDtoBuilder playerId(Long playerId) {
            this.playerId = playerId;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder canonicalName(String canonicalName) {
            this.canonicalName = canonicalName;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder position(String position) {
            this.position = position;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder nationality(String nationality) {
            this.nationality = nationality;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder sofascoreId(Long sofascoreId) {
            this.sofascoreId = sofascoreId;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder archetype(String archetype) {
            this.archetype = archetype;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder overallRating(BigDecimal overallRating) {
            this.overallRating = overallRating;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder isJokerCandidate(Boolean isJokerCandidate) {
            this.isJokerCandidate = isJokerCandidate;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder preferredFoot(String preferredFoot) {
            this.preferredFoot = preferredFoot;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder paceRating(BigDecimal paceRating) {
            this.paceRating = paceRating;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder shootingRating(BigDecimal shootingRating) {
            this.shootingRating = shootingRating;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder passingRating(BigDecimal passingRating) {
            this.passingRating = passingRating;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder referenceTier(Integer referenceTier) {
            this.referenceTier = referenceTier;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder similarPlayers(Object similarPlayers) {
            this.similarPlayers = similarPlayers;
            return this;
        }

        @Generated
        public PlayerProfileDtoBuilder memberships(List<ActiveMembershipDto> memberships) {
            this.memberships = memberships;
            return this;
        }

        @Generated
        public PlayerProfileDto build() {
            return new PlayerProfileDto(this.playerId, this.canonicalName, this.slug, this.position, this.nationality, this.sofascoreId, this.archetype, this.overallRating, this.isJokerCandidate, this.preferredFoot, this.paceRating, this.shootingRating, this.passingRating, this.referenceTier, this.similarPlayers, this.memberships);
        }

        @Generated
        public String toString() {
            return "PlayerProfileDto.PlayerProfileDtoBuilder(playerId=" + this.playerId + ", canonicalName=" + this.canonicalName + ", slug=" + this.slug + ", position=" + this.position + ", nationality=" + this.nationality + ", sofascoreId=" + this.sofascoreId + ", archetype=" + this.archetype + ", overallRating=" + String.valueOf(this.overallRating) + ", isJokerCandidate=" + this.isJokerCandidate + ", preferredFoot=" + this.preferredFoot + ", paceRating=" + String.valueOf(this.paceRating) + ", shootingRating=" + String.valueOf(this.shootingRating) + ", passingRating=" + String.valueOf(this.passingRating) + ", referenceTier=" + this.referenceTier + ", similarPlayers=" + String.valueOf(this.similarPlayers) + ", memberships=" + String.valueOf(this.memberships) + ")";
        }
    }

    public static class ActiveMembershipDto {
        private Long teamId;
        private String teamName;
        private String teamSlug;
        private Boolean isNationalTeam;
        private String membershipType;
        private Integer shirtNumber;

        @Generated
        public static ActiveMembershipDtoBuilder builder() {
            return new ActiveMembershipDtoBuilder();
        }

        @Generated
        public Long getTeamId() {
            return this.teamId;
        }

        @Generated
        public String getTeamName() {
            return this.teamName;
        }

        @Generated
        public String getTeamSlug() {
            return this.teamSlug;
        }

        @Generated
        public Boolean getIsNationalTeam() {
            return this.isNationalTeam;
        }

        @Generated
        public String getMembershipType() {
            return this.membershipType;
        }

        @Generated
        public Integer getShirtNumber() {
            return this.shirtNumber;
        }

        @Generated
        public void setTeamId(Long teamId) {
            this.teamId = teamId;
        }

        @Generated
        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        @Generated
        public void setTeamSlug(String teamSlug) {
            this.teamSlug = teamSlug;
        }

        @Generated
        public void setIsNationalTeam(Boolean isNationalTeam) {
            this.isNationalTeam = isNationalTeam;
        }

        @Generated
        public void setMembershipType(String membershipType) {
            this.membershipType = membershipType;
        }

        @Generated
        public void setShirtNumber(Integer shirtNumber) {
            this.shirtNumber = shirtNumber;
        }

        @Generated
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ActiveMembershipDto)) {
                return false;
            }
            ActiveMembershipDto other = (ActiveMembershipDto)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Long this$teamId = this.getTeamId();
            Long other$teamId = other.getTeamId();
            if (this$teamId == null ? other$teamId != null : !((Object)this$teamId).equals(other$teamId)) {
                return false;
            }
            Boolean this$isNationalTeam = this.getIsNationalTeam();
            Boolean other$isNationalTeam = other.getIsNationalTeam();
            if (this$isNationalTeam == null ? other$isNationalTeam != null : !((Object)this$isNationalTeam).equals(other$isNationalTeam)) {
                return false;
            }
            Integer this$shirtNumber = this.getShirtNumber();
            Integer other$shirtNumber = other.getShirtNumber();
            if (this$shirtNumber == null ? other$shirtNumber != null : !((Object)this$shirtNumber).equals(other$shirtNumber)) {
                return false;
            }
            String this$teamName = this.getTeamName();
            String other$teamName = other.getTeamName();
            if (this$teamName == null ? other$teamName != null : !this$teamName.equals(other$teamName)) {
                return false;
            }
            String this$teamSlug = this.getTeamSlug();
            String other$teamSlug = other.getTeamSlug();
            if (this$teamSlug == null ? other$teamSlug != null : !this$teamSlug.equals(other$teamSlug)) {
                return false;
            }
            String this$membershipType = this.getMembershipType();
            String other$membershipType = other.getMembershipType();
            return !(this$membershipType == null ? other$membershipType != null : !this$membershipType.equals(other$membershipType));
        }

        @Generated
        protected boolean canEqual(Object other) {
            return other instanceof ActiveMembershipDto;
        }

        @Generated
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Long $teamId = this.getTeamId();
            result = result * 59 + ($teamId == null ? 43 : ((Object)$teamId).hashCode());
            Boolean $isNationalTeam = this.getIsNationalTeam();
            result = result * 59 + ($isNationalTeam == null ? 43 : ((Object)$isNationalTeam).hashCode());
            Integer $shirtNumber = this.getShirtNumber();
            result = result * 59 + ($shirtNumber == null ? 43 : ((Object)$shirtNumber).hashCode());
            String $teamName = this.getTeamName();
            result = result * 59 + ($teamName == null ? 43 : $teamName.hashCode());
            String $teamSlug = this.getTeamSlug();
            result = result * 59 + ($teamSlug == null ? 43 : $teamSlug.hashCode());
            String $membershipType = this.getMembershipType();
            result = result * 59 + ($membershipType == null ? 43 : $membershipType.hashCode());
            return result;
        }

        @Generated
        public String toString() {
            return "PlayerProfileDto.ActiveMembershipDto(teamId=" + this.getTeamId() + ", teamName=" + this.getTeamName() + ", teamSlug=" + this.getTeamSlug() + ", isNationalTeam=" + this.getIsNationalTeam() + ", membershipType=" + this.getMembershipType() + ", shirtNumber=" + this.getShirtNumber() + ")";
        }

        @Generated
        public ActiveMembershipDto() {
        }

        @Generated
        public ActiveMembershipDto(Long teamId, String teamName, String teamSlug, Boolean isNationalTeam, String membershipType, Integer shirtNumber) {
            this.teamId = teamId;
            this.teamName = teamName;
            this.teamSlug = teamSlug;
            this.isNationalTeam = isNationalTeam;
            this.membershipType = membershipType;
            this.shirtNumber = shirtNumber;
        }

        @Generated
        public static class ActiveMembershipDtoBuilder {
            @Generated
            private Long teamId;
            @Generated
            private String teamName;
            @Generated
            private String teamSlug;
            @Generated
            private Boolean isNationalTeam;
            @Generated
            private String membershipType;
            @Generated
            private Integer shirtNumber;

            @Generated
            ActiveMembershipDtoBuilder() {
            }

            @Generated
            public ActiveMembershipDtoBuilder teamId(Long teamId) {
                this.teamId = teamId;
                return this;
            }

            @Generated
            public ActiveMembershipDtoBuilder teamName(String teamName) {
                this.teamName = teamName;
                return this;
            }

            @Generated
            public ActiveMembershipDtoBuilder teamSlug(String teamSlug) {
                this.teamSlug = teamSlug;
                return this;
            }

            @Generated
            public ActiveMembershipDtoBuilder isNationalTeam(Boolean isNationalTeam) {
                this.isNationalTeam = isNationalTeam;
                return this;
            }

            @Generated
            public ActiveMembershipDtoBuilder membershipType(String membershipType) {
                this.membershipType = membershipType;
                return this;
            }

            @Generated
            public ActiveMembershipDtoBuilder shirtNumber(Integer shirtNumber) {
                this.shirtNumber = shirtNumber;
                return this;
            }

            @Generated
            public ActiveMembershipDto build() {
                return new ActiveMembershipDto(this.teamId, this.teamName, this.teamSlug, this.isNationalTeam, this.membershipType, this.shirtNumber);
            }

            @Generated
            public String toString() {
                return "PlayerProfileDto.ActiveMembershipDto.ActiveMembershipDtoBuilder(teamId=" + this.teamId + ", teamName=" + this.teamName + ", teamSlug=" + this.teamSlug + ", isNationalTeam=" + this.isNationalTeam + ", membershipType=" + this.membershipType + ", shirtNumber=" + this.shirtNumber + ")";
            }
        }
    }
}

