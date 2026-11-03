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
    private TeamDnaDto dna;
    private TeamSofaMetadataDto sofaMetadata;
    private List<SquadMemberDto> squad;

    @Generated
    public static TeamProfileDtoBuilder builder() {
        return new TeamProfileDtoBuilder();
    }

    @Generated
    public Long getTeamId() {
        return this.teamId;
    }

    @Generated
    public Integer getSportId() {
        return this.sportId;
    }

    @Generated
    public String getSportSlug() {
        return this.sportSlug;
    }

    @Generated
    public String getSlug() {
        return this.slug;
    }

    @Generated
    public String getCanonicalName() {
        return this.canonicalName;
    }

    @Generated
    public String getLogoUrl() {
        return this.logoUrl;
    }

    @Generated
    public String getGender() {
        return this.gender;
    }

    @Generated
    public String getAgeCategory() {
        return this.ageCategory;
    }

    @Generated
    public Boolean getIsNational() {
        return this.isNational;
    }

    @Generated
    public Boolean getIsIndividual() {
        return this.isIndividual;
    }

    @Generated
    public Long getSofascoreId() {
        return this.sofascoreId;
    }

    @Generated
    public TeamDnaDto getDna() {
        return this.dna;
    }

    @Generated
    public TeamSofaMetadataDto getSofaMetadata() {
        return this.sofaMetadata;
    }

    @Generated
    public List<SquadMemberDto> getSquad() {
        return this.squad;
    }

    @Generated
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    @Generated
    public void setSportId(Integer sportId) {
        this.sportId = sportId;
    }

    @Generated
    public void setSportSlug(String sportSlug) {
        this.sportSlug = sportSlug;
    }

    @Generated
    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Generated
    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    @Generated
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    @Generated
    public void setGender(String gender) {
        this.gender = gender;
    }

    @Generated
    public void setAgeCategory(String ageCategory) {
        this.ageCategory = ageCategory;
    }

    @Generated
    public void setIsNational(Boolean isNational) {
        this.isNational = isNational;
    }

    @Generated
    public void setIsIndividual(Boolean isIndividual) {
        this.isIndividual = isIndividual;
    }

    @Generated
    public void setSofascoreId(Long sofascoreId) {
        this.sofascoreId = sofascoreId;
    }

    @Generated
    public void setDna(TeamDnaDto dna) {
        this.dna = dna;
    }

    @Generated
    public void setSofaMetadata(TeamSofaMetadataDto sofaMetadata) {
        this.sofaMetadata = sofaMetadata;
    }

    @Generated
    public void setSquad(List<SquadMemberDto> squad) {
        this.squad = squad;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TeamProfileDto)) {
            return false;
        }
        TeamProfileDto other = (TeamProfileDto)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$teamId = this.getTeamId();
        Long other$teamId = other.getTeamId();
        if (this$teamId == null ? other$teamId != null : !((Object)this$teamId).equals(other$teamId)) {
            return false;
        }
        Integer this$sportId = this.getSportId();
        Integer other$sportId = other.getSportId();
        if (this$sportId == null ? other$sportId != null : !((Object)this$sportId).equals(other$sportId)) {
            return false;
        }
        Boolean this$isNational = this.getIsNational();
        Boolean other$isNational = other.getIsNational();
        if (this$isNational == null ? other$isNational != null : !((Object)this$isNational).equals(other$isNational)) {
            return false;
        }
        Boolean this$isIndividual = this.getIsIndividual();
        Boolean other$isIndividual = other.getIsIndividual();
        if (this$isIndividual == null ? other$isIndividual != null : !((Object)this$isIndividual).equals(other$isIndividual)) {
            return false;
        }
        Long this$sofascoreId = this.getSofascoreId();
        Long other$sofascoreId = other.getSofascoreId();
        if (this$sofascoreId == null ? other$sofascoreId != null : !((Object)this$sofascoreId).equals(other$sofascoreId)) {
            return false;
        }
        String this$sportSlug = this.getSportSlug();
        String other$sportSlug = other.getSportSlug();
        if (this$sportSlug == null ? other$sportSlug != null : !this$sportSlug.equals(other$sportSlug)) {
            return false;
        }
        String this$slug = this.getSlug();
        String other$slug = other.getSlug();
        if (this$slug == null ? other$slug != null : !this$slug.equals(other$slug)) {
            return false;
        }
        String this$canonicalName = this.getCanonicalName();
        String other$canonicalName = other.getCanonicalName();
        if (this$canonicalName == null ? other$canonicalName != null : !this$canonicalName.equals(other$canonicalName)) {
            return false;
        }
        String this$logoUrl = this.getLogoUrl();
        String other$logoUrl = other.getLogoUrl();
        if (this$logoUrl == null ? other$logoUrl != null : !this$logoUrl.equals(other$logoUrl)) {
            return false;
        }
        String this$gender = this.getGender();
        String other$gender = other.getGender();
        if (this$gender == null ? other$gender != null : !this$gender.equals(other$gender)) {
            return false;
        }
        String this$ageCategory = this.getAgeCategory();
        String other$ageCategory = other.getAgeCategory();
        if (this$ageCategory == null ? other$ageCategory != null : !this$ageCategory.equals(other$ageCategory)) {
            return false;
        }
        TeamDnaDto this$dna = this.getDna();
        TeamDnaDto other$dna = other.getDna();
        if (this$dna == null ? other$dna != null : !((Object)this$dna).equals(other$dna)) {
            return false;
        }
        TeamSofaMetadataDto this$sofaMetadata = this.getSofaMetadata();
        TeamSofaMetadataDto other$sofaMetadata = other.getSofaMetadata();
        if (this$sofaMetadata == null ? other$sofaMetadata != null : !((Object)this$sofaMetadata).equals(other$sofaMetadata)) {
            return false;
        }
        List<SquadMemberDto> this$squad = this.getSquad();
        List<SquadMemberDto> other$squad = other.getSquad();
        return !(this$squad == null ? other$squad != null : !((Object)this$squad).equals(other$squad));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof TeamProfileDto;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $teamId = this.getTeamId();
        result = result * 59 + ($teamId == null ? 43 : ((Object)$teamId).hashCode());
        Integer $sportId = this.getSportId();
        result = result * 59 + ($sportId == null ? 43 : ((Object)$sportId).hashCode());
        Boolean $isNational = this.getIsNational();
        result = result * 59 + ($isNational == null ? 43 : ((Object)$isNational).hashCode());
        Boolean $isIndividual = this.getIsIndividual();
        result = result * 59 + ($isIndividual == null ? 43 : ((Object)$isIndividual).hashCode());
        Long $sofascoreId = this.getSofascoreId();
        result = result * 59 + ($sofascoreId == null ? 43 : ((Object)$sofascoreId).hashCode());
        String $sportSlug = this.getSportSlug();
        result = result * 59 + ($sportSlug == null ? 43 : $sportSlug.hashCode());
        String $slug = this.getSlug();
        result = result * 59 + ($slug == null ? 43 : $slug.hashCode());
        String $canonicalName = this.getCanonicalName();
        result = result * 59 + ($canonicalName == null ? 43 : $canonicalName.hashCode());
        String $logoUrl = this.getLogoUrl();
        result = result * 59 + ($logoUrl == null ? 43 : $logoUrl.hashCode());
        String $gender = this.getGender();
        result = result * 59 + ($gender == null ? 43 : $gender.hashCode());
        String $ageCategory = this.getAgeCategory();
        result = result * 59 + ($ageCategory == null ? 43 : $ageCategory.hashCode());
        TeamDnaDto $dna = this.getDna();
        result = result * 59 + ($dna == null ? 43 : ((Object)$dna).hashCode());
        TeamSofaMetadataDto $sofaMetadata = this.getSofaMetadata();
        result = result * 59 + ($sofaMetadata == null ? 43 : ((Object)$sofaMetadata).hashCode());
        List<SquadMemberDto> $squad = this.getSquad();
        result = result * 59 + ($squad == null ? 43 : ((Object)$squad).hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "TeamProfileDto(teamId=" + this.getTeamId() + ", sportId=" + this.getSportId() + ", sportSlug=" + this.getSportSlug() + ", slug=" + this.getSlug() + ", canonicalName=" + this.getCanonicalName() + ", logoUrl=" + this.getLogoUrl() + ", gender=" + this.getGender() + ", ageCategory=" + this.getAgeCategory() + ", isNational=" + this.getIsNational() + ", isIndividual=" + this.getIsIndividual() + ", sofascoreId=" + this.getSofascoreId() + ", dna=" + String.valueOf(this.getDna()) + ", sofaMetadata=" + String.valueOf(this.getSofaMetadata()) + ", squad=" + String.valueOf(this.getSquad()) + ")";
    }

    @Generated
    public TeamProfileDto() {
    }

    @Generated
    public TeamProfileDto(Long teamId, Integer sportId, String sportSlug, String slug, String canonicalName, String logoUrl, String gender, String ageCategory, Boolean isNational, Boolean isIndividual, Long sofascoreId, TeamDnaDto dna, TeamSofaMetadataDto sofaMetadata, List<SquadMemberDto> squad) {
        this.teamId = teamId;
        this.sportId = sportId;
        this.sportSlug = sportSlug;
        this.slug = slug;
        this.canonicalName = canonicalName;
        this.logoUrl = logoUrl;
        this.gender = gender;
        this.ageCategory = ageCategory;
        this.isNational = isNational;
        this.isIndividual = isIndividual;
        this.sofascoreId = sofascoreId;
        this.dna = dna;
        this.sofaMetadata = sofaMetadata;
        this.squad = squad;
    }

    @Generated
    public static class TeamProfileDtoBuilder {
        @Generated
        private Long teamId;
        @Generated
        private Integer sportId;
        @Generated
        private String sportSlug;
        @Generated
        private String slug;
        @Generated
        private String canonicalName;
        @Generated
        private String logoUrl;
        @Generated
        private String gender;
        @Generated
        private String ageCategory;
        @Generated
        private Boolean isNational;
        @Generated
        private Boolean isIndividual;
        @Generated
        private Long sofascoreId;
        @Generated
        private TeamDnaDto dna;
        @Generated
        private TeamSofaMetadataDto sofaMetadata;
        @Generated
        private List<SquadMemberDto> squad;

        @Generated
        TeamProfileDtoBuilder() {
        }

        @Generated
        public TeamProfileDtoBuilder teamId(Long teamId) {
            this.teamId = teamId;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder sportId(Integer sportId) {
            this.sportId = sportId;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder sportSlug(String sportSlug) {
            this.sportSlug = sportSlug;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder canonicalName(String canonicalName) {
            this.canonicalName = canonicalName;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder logoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder gender(String gender) {
            this.gender = gender;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder ageCategory(String ageCategory) {
            this.ageCategory = ageCategory;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder isNational(Boolean isNational) {
            this.isNational = isNational;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder isIndividual(Boolean isIndividual) {
            this.isIndividual = isIndividual;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder sofascoreId(Long sofascoreId) {
            this.sofascoreId = sofascoreId;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder dna(TeamDnaDto dna) {
            this.dna = dna;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder sofaMetadata(TeamSofaMetadataDto sofaMetadata) {
            this.sofaMetadata = sofaMetadata;
            return this;
        }

        @Generated
        public TeamProfileDtoBuilder squad(List<SquadMemberDto> squad) {
            this.squad = squad;
            return this;
        }

        @Generated
        public TeamProfileDto build() {
            return new TeamProfileDto(this.teamId, this.sportId, this.sportSlug, this.slug, this.canonicalName, this.logoUrl, this.gender, this.ageCategory, this.isNational, this.isIndividual, this.sofascoreId, this.dna, this.sofaMetadata, this.squad);
        }

        @Generated
        public String toString() {
            return "TeamProfileDto.TeamProfileDtoBuilder(teamId=" + this.teamId + ", sportId=" + this.sportId + ", sportSlug=" + this.sportSlug + ", slug=" + this.slug + ", canonicalName=" + this.canonicalName + ", logoUrl=" + this.logoUrl + ", gender=" + this.gender + ", ageCategory=" + this.ageCategory + ", isNational=" + this.isNational + ", isIndividual=" + this.isIndividual + ", sofascoreId=" + this.sofascoreId + ", dna=" + String.valueOf(this.dna) + ", sofaMetadata=" + String.valueOf(this.sofaMetadata) + ", squad=" + String.valueOf(this.squad) + ")";
        }
    }

    public static class TeamDnaDto {
        private String primaryFormation;
        private String playStyle;
        private BigDecimal pressingIntensity;
        private BigDecimal possessionAvgPct;
        private BigDecimal aerialThreatScore;
        private BigDecimal defenseVulnerabilityScore;
        private Integer referenceTier;
        private Object similarTeams;

        @Generated
        public static TeamDnaDtoBuilder builder() {
            return new TeamDnaDtoBuilder();
        }

        @Generated
        public String getPrimaryFormation() {
            return this.primaryFormation;
        }

        @Generated
        public String getPlayStyle() {
            return this.playStyle;
        }

        @Generated
        public BigDecimal getPressingIntensity() {
            return this.pressingIntensity;
        }

        @Generated
        public BigDecimal getPossessionAvgPct() {
            return this.possessionAvgPct;
        }

        @Generated
        public BigDecimal getAerialThreatScore() {
            return this.aerialThreatScore;
        }

        @Generated
        public BigDecimal getDefenseVulnerabilityScore() {
            return this.defenseVulnerabilityScore;
        }

        @Generated
        public Integer getReferenceTier() {
            return this.referenceTier;
        }

        @Generated
        public Object getSimilarTeams() {
            return this.similarTeams;
        }

        @Generated
        public void setPrimaryFormation(String primaryFormation) {
            this.primaryFormation = primaryFormation;
        }

        @Generated
        public void setPlayStyle(String playStyle) {
            this.playStyle = playStyle;
        }

        @Generated
        public void setPressingIntensity(BigDecimal pressingIntensity) {
            this.pressingIntensity = pressingIntensity;
        }

        @Generated
        public void setPossessionAvgPct(BigDecimal possessionAvgPct) {
            this.possessionAvgPct = possessionAvgPct;
        }

        @Generated
        public void setAerialThreatScore(BigDecimal aerialThreatScore) {
            this.aerialThreatScore = aerialThreatScore;
        }

        @Generated
        public void setDefenseVulnerabilityScore(BigDecimal defenseVulnerabilityScore) {
            this.defenseVulnerabilityScore = defenseVulnerabilityScore;
        }

        @Generated
        public void setReferenceTier(Integer referenceTier) {
            this.referenceTier = referenceTier;
        }

        @Generated
        public void setSimilarTeams(Object similarTeams) {
            this.similarTeams = similarTeams;
        }

        @Generated
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof TeamDnaDto)) {
                return false;
            }
            TeamDnaDto other = (TeamDnaDto)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Integer this$referenceTier = this.getReferenceTier();
            Integer other$referenceTier = other.getReferenceTier();
            if (this$referenceTier == null ? other$referenceTier != null : !((Object)this$referenceTier).equals(other$referenceTier)) {
                return false;
            }
            String this$primaryFormation = this.getPrimaryFormation();
            String other$primaryFormation = other.getPrimaryFormation();
            if (this$primaryFormation == null ? other$primaryFormation != null : !this$primaryFormation.equals(other$primaryFormation)) {
                return false;
            }
            String this$playStyle = this.getPlayStyle();
            String other$playStyle = other.getPlayStyle();
            if (this$playStyle == null ? other$playStyle != null : !this$playStyle.equals(other$playStyle)) {
                return false;
            }
            BigDecimal this$pressingIntensity = this.getPressingIntensity();
            BigDecimal other$pressingIntensity = other.getPressingIntensity();
            if (this$pressingIntensity == null ? other$pressingIntensity != null : !((Object)this$pressingIntensity).equals(other$pressingIntensity)) {
                return false;
            }
            BigDecimal this$possessionAvgPct = this.getPossessionAvgPct();
            BigDecimal other$possessionAvgPct = other.getPossessionAvgPct();
            if (this$possessionAvgPct == null ? other$possessionAvgPct != null : !((Object)this$possessionAvgPct).equals(other$possessionAvgPct)) {
                return false;
            }
            BigDecimal this$aerialThreatScore = this.getAerialThreatScore();
            BigDecimal other$aerialThreatScore = other.getAerialThreatScore();
            if (this$aerialThreatScore == null ? other$aerialThreatScore != null : !((Object)this$aerialThreatScore).equals(other$aerialThreatScore)) {
                return false;
            }
            BigDecimal this$defenseVulnerabilityScore = this.getDefenseVulnerabilityScore();
            BigDecimal other$defenseVulnerabilityScore = other.getDefenseVulnerabilityScore();
            if (this$defenseVulnerabilityScore == null ? other$defenseVulnerabilityScore != null : !((Object)this$defenseVulnerabilityScore).equals(other$defenseVulnerabilityScore)) {
                return false;
            }
            Object this$similarTeams = this.getSimilarTeams();
            Object other$similarTeams = other.getSimilarTeams();
            return !(this$similarTeams == null ? other$similarTeams != null : !this$similarTeams.equals(other$similarTeams));
        }

        @Generated
        protected boolean canEqual(Object other) {
            return other instanceof TeamDnaDto;
        }

        @Generated
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Integer $referenceTier = this.getReferenceTier();
            result = result * 59 + ($referenceTier == null ? 43 : ((Object)$referenceTier).hashCode());
            String $primaryFormation = this.getPrimaryFormation();
            result = result * 59 + ($primaryFormation == null ? 43 : $primaryFormation.hashCode());
            String $playStyle = this.getPlayStyle();
            result = result * 59 + ($playStyle == null ? 43 : $playStyle.hashCode());
            BigDecimal $pressingIntensity = this.getPressingIntensity();
            result = result * 59 + ($pressingIntensity == null ? 43 : ((Object)$pressingIntensity).hashCode());
            BigDecimal $possessionAvgPct = this.getPossessionAvgPct();
            result = result * 59 + ($possessionAvgPct == null ? 43 : ((Object)$possessionAvgPct).hashCode());
            BigDecimal $aerialThreatScore = this.getAerialThreatScore();
            result = result * 59 + ($aerialThreatScore == null ? 43 : ((Object)$aerialThreatScore).hashCode());
            BigDecimal $defenseVulnerabilityScore = this.getDefenseVulnerabilityScore();
            result = result * 59 + ($defenseVulnerabilityScore == null ? 43 : ((Object)$defenseVulnerabilityScore).hashCode());
            Object $similarTeams = this.getSimilarTeams();
            result = result * 59 + ($similarTeams == null ? 43 : $similarTeams.hashCode());
            return result;
        }

        @Generated
        public String toString() {
            return "TeamProfileDto.TeamDnaDto(primaryFormation=" + this.getPrimaryFormation() + ", playStyle=" + this.getPlayStyle() + ", pressingIntensity=" + String.valueOf(this.getPressingIntensity()) + ", possessionAvgPct=" + String.valueOf(this.getPossessionAvgPct()) + ", aerialThreatScore=" + String.valueOf(this.getAerialThreatScore()) + ", defenseVulnerabilityScore=" + String.valueOf(this.getDefenseVulnerabilityScore()) + ", referenceTier=" + this.getReferenceTier() + ", similarTeams=" + String.valueOf(this.getSimilarTeams()) + ")";
        }

        @Generated
        public TeamDnaDto() {
        }

        @Generated
        public TeamDnaDto(String primaryFormation, String playStyle, BigDecimal pressingIntensity, BigDecimal possessionAvgPct, BigDecimal aerialThreatScore, BigDecimal defenseVulnerabilityScore, Integer referenceTier, Object similarTeams) {
            this.primaryFormation = primaryFormation;
            this.playStyle = playStyle;
            this.pressingIntensity = pressingIntensity;
            this.possessionAvgPct = possessionAvgPct;
            this.aerialThreatScore = aerialThreatScore;
            this.defenseVulnerabilityScore = defenseVulnerabilityScore;
            this.referenceTier = referenceTier;
            this.similarTeams = similarTeams;
        }

        @Generated
        public static class TeamDnaDtoBuilder {
            @Generated
            private String primaryFormation;
            @Generated
            private String playStyle;
            @Generated
            private BigDecimal pressingIntensity;
            @Generated
            private BigDecimal possessionAvgPct;
            @Generated
            private BigDecimal aerialThreatScore;
            @Generated
            private BigDecimal defenseVulnerabilityScore;
            @Generated
            private Integer referenceTier;
            @Generated
            private Object similarTeams;

            @Generated
            TeamDnaDtoBuilder() {
            }

            @Generated
            public TeamDnaDtoBuilder primaryFormation(String primaryFormation) {
                this.primaryFormation = primaryFormation;
                return this;
            }

            @Generated
            public TeamDnaDtoBuilder playStyle(String playStyle) {
                this.playStyle = playStyle;
                return this;
            }

            @Generated
            public TeamDnaDtoBuilder pressingIntensity(BigDecimal pressingIntensity) {
                this.pressingIntensity = pressingIntensity;
                return this;
            }

            @Generated
            public TeamDnaDtoBuilder possessionAvgPct(BigDecimal possessionAvgPct) {
                this.possessionAvgPct = possessionAvgPct;
                return this;
            }

            @Generated
            public TeamDnaDtoBuilder aerialThreatScore(BigDecimal aerialThreatScore) {
                this.aerialThreatScore = aerialThreatScore;
                return this;
            }

            @Generated
            public TeamDnaDtoBuilder defenseVulnerabilityScore(BigDecimal defenseVulnerabilityScore) {
                this.defenseVulnerabilityScore = defenseVulnerabilityScore;
                return this;
            }

            @Generated
            public TeamDnaDtoBuilder referenceTier(Integer referenceTier) {
                this.referenceTier = referenceTier;
                return this;
            }

            @Generated
            public TeamDnaDtoBuilder similarTeams(Object similarTeams) {
                this.similarTeams = similarTeams;
                return this;
            }

            @Generated
            public TeamDnaDto build() {
                return new TeamDnaDto(this.primaryFormation, this.playStyle, this.pressingIntensity, this.possessionAvgPct, this.aerialThreatScore, this.defenseVulnerabilityScore, this.referenceTier, this.similarTeams);
            }

            @Generated
            public String toString() {
                return "TeamProfileDto.TeamDnaDto.TeamDnaDtoBuilder(primaryFormation=" + this.primaryFormation + ", playStyle=" + this.playStyle + ", pressingIntensity=" + String.valueOf(this.pressingIntensity) + ", possessionAvgPct=" + String.valueOf(this.possessionAvgPct) + ", aerialThreatScore=" + String.valueOf(this.aerialThreatScore) + ", defenseVulnerabilityScore=" + String.valueOf(this.defenseVulnerabilityScore) + ", referenceTier=" + this.referenceTier + ", similarTeams=" + String.valueOf(this.similarTeams) + ")";
            }
        }
    }

    public static class TeamSofaMetadataDto {
        private String venueName;
        private String venueCity;
        private Integer venueCapacity;
        private String coachName;
        private BigDecimal sofascoreRating;
        private String formRecent;

        @Generated
        public static TeamSofaMetadataDtoBuilder builder() {
            return new TeamSofaMetadataDtoBuilder();
        }

        @Generated
        public String getVenueName() {
            return this.venueName;
        }

        @Generated
        public String getVenueCity() {
            return this.venueCity;
        }

        @Generated
        public Integer getVenueCapacity() {
            return this.venueCapacity;
        }

        @Generated
        public String getCoachName() {
            return this.coachName;
        }

        @Generated
        public BigDecimal getSofascoreRating() {
            return this.sofascoreRating;
        }

        @Generated
        public String getFormRecent() {
            return this.formRecent;
        }

        @Generated
        public void setVenueName(String venueName) {
            this.venueName = venueName;
        }

        @Generated
        public void setVenueCity(String venueCity) {
            this.venueCity = venueCity;
        }

        @Generated
        public void setVenueCapacity(Integer venueCapacity) {
            this.venueCapacity = venueCapacity;
        }

        @Generated
        public void setCoachName(String coachName) {
            this.coachName = coachName;
        }

        @Generated
        public void setSofascoreRating(BigDecimal sofascoreRating) {
            this.sofascoreRating = sofascoreRating;
        }

        @Generated
        public void setFormRecent(String formRecent) {
            this.formRecent = formRecent;
        }

        @Generated
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof TeamSofaMetadataDto)) {
                return false;
            }
            TeamSofaMetadataDto other = (TeamSofaMetadataDto)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Integer this$venueCapacity = this.getVenueCapacity();
            Integer other$venueCapacity = other.getVenueCapacity();
            if (this$venueCapacity == null ? other$venueCapacity != null : !((Object)this$venueCapacity).equals(other$venueCapacity)) {
                return false;
            }
            String this$venueName = this.getVenueName();
            String other$venueName = other.getVenueName();
            if (this$venueName == null ? other$venueName != null : !this$venueName.equals(other$venueName)) {
                return false;
            }
            String this$venueCity = this.getVenueCity();
            String other$venueCity = other.getVenueCity();
            if (this$venueCity == null ? other$venueCity != null : !this$venueCity.equals(other$venueCity)) {
                return false;
            }
            String this$coachName = this.getCoachName();
            String other$coachName = other.getCoachName();
            if (this$coachName == null ? other$coachName != null : !this$coachName.equals(other$coachName)) {
                return false;
            }
            BigDecimal this$sofascoreRating = this.getSofascoreRating();
            BigDecimal other$sofascoreRating = other.getSofascoreRating();
            if (this$sofascoreRating == null ? other$sofascoreRating != null : !((Object)this$sofascoreRating).equals(other$sofascoreRating)) {
                return false;
            }
            String this$formRecent = this.getFormRecent();
            String other$formRecent = other.getFormRecent();
            return !(this$formRecent == null ? other$formRecent != null : !this$formRecent.equals(other$formRecent));
        }

        @Generated
        protected boolean canEqual(Object other) {
            return other instanceof TeamSofaMetadataDto;
        }

        @Generated
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Integer $venueCapacity = this.getVenueCapacity();
            result = result * 59 + ($venueCapacity == null ? 43 : ((Object)$venueCapacity).hashCode());
            String $venueName = this.getVenueName();
            result = result * 59 + ($venueName == null ? 43 : $venueName.hashCode());
            String $venueCity = this.getVenueCity();
            result = result * 59 + ($venueCity == null ? 43 : $venueCity.hashCode());
            String $coachName = this.getCoachName();
            result = result * 59 + ($coachName == null ? 43 : $coachName.hashCode());
            BigDecimal $sofascoreRating = this.getSofascoreRating();
            result = result * 59 + ($sofascoreRating == null ? 43 : ((Object)$sofascoreRating).hashCode());
            String $formRecent = this.getFormRecent();
            result = result * 59 + ($formRecent == null ? 43 : $formRecent.hashCode());
            return result;
        }

        @Generated
        public String toString() {
            return "TeamProfileDto.TeamSofaMetadataDto(venueName=" + this.getVenueName() + ", venueCity=" + this.getVenueCity() + ", venueCapacity=" + this.getVenueCapacity() + ", coachName=" + this.getCoachName() + ", sofascoreRating=" + String.valueOf(this.getSofascoreRating()) + ", formRecent=" + this.getFormRecent() + ")";
        }

        @Generated
        public TeamSofaMetadataDto() {
        }

        @Generated
        public TeamSofaMetadataDto(String venueName, String venueCity, Integer venueCapacity, String coachName, BigDecimal sofascoreRating, String formRecent) {
            this.venueName = venueName;
            this.venueCity = venueCity;
            this.venueCapacity = venueCapacity;
            this.coachName = coachName;
            this.sofascoreRating = sofascoreRating;
            this.formRecent = formRecent;
        }

        @Generated
        public static class TeamSofaMetadataDtoBuilder {
            @Generated
            private String venueName;
            @Generated
            private String venueCity;
            @Generated
            private Integer venueCapacity;
            @Generated
            private String coachName;
            @Generated
            private BigDecimal sofascoreRating;
            @Generated
            private String formRecent;

            @Generated
            TeamSofaMetadataDtoBuilder() {
            }

            @Generated
            public TeamSofaMetadataDtoBuilder venueName(String venueName) {
                this.venueName = venueName;
                return this;
            }

            @Generated
            public TeamSofaMetadataDtoBuilder venueCity(String venueCity) {
                this.venueCity = venueCity;
                return this;
            }

            @Generated
            public TeamSofaMetadataDtoBuilder venueCapacity(Integer venueCapacity) {
                this.venueCapacity = venueCapacity;
                return this;
            }

            @Generated
            public TeamSofaMetadataDtoBuilder coachName(String coachName) {
                this.coachName = coachName;
                return this;
            }

            @Generated
            public TeamSofaMetadataDtoBuilder sofascoreRating(BigDecimal sofascoreRating) {
                this.sofascoreRating = sofascoreRating;
                return this;
            }

            @Generated
            public TeamSofaMetadataDtoBuilder formRecent(String formRecent) {
                this.formRecent = formRecent;
                return this;
            }

            @Generated
            public TeamSofaMetadataDto build() {
                return new TeamSofaMetadataDto(this.venueName, this.venueCity, this.venueCapacity, this.coachName, this.sofascoreRating, this.formRecent);
            }

            @Generated
            public String toString() {
                return "TeamProfileDto.TeamSofaMetadataDto.TeamSofaMetadataDtoBuilder(venueName=" + this.venueName + ", venueCity=" + this.venueCity + ", venueCapacity=" + this.venueCapacity + ", coachName=" + this.coachName + ", sofascoreRating=" + String.valueOf(this.sofascoreRating) + ", formRecent=" + this.formRecent + ")";
            }
        }
    }

    public static class SquadMemberDto {
        private Long playerId;
        private String playerName;
        private String playerSlug;
        private String position;
        private Integer shirtNumber;
        private String membershipType;
        private Boolean isJokerCandidate;
        private BigDecimal overallRating;
        private String archetype;
        private Object similarPlayers;

        @Generated
        public static SquadMemberDtoBuilder builder() {
            return new SquadMemberDtoBuilder();
        }

        @Generated
        public Long getPlayerId() {
            return this.playerId;
        }

        @Generated
        public String getPlayerName() {
            return this.playerName;
        }

        @Generated
        public String getPlayerSlug() {
            return this.playerSlug;
        }

        @Generated
        public String getPosition() {
            return this.position;
        }

        @Generated
        public Integer getShirtNumber() {
            return this.shirtNumber;
        }

        @Generated
        public String getMembershipType() {
            return this.membershipType;
        }

        @Generated
        public Boolean getIsJokerCandidate() {
            return this.isJokerCandidate;
        }

        @Generated
        public BigDecimal getOverallRating() {
            return this.overallRating;
        }

        @Generated
        public String getArchetype() {
            return this.archetype;
        }

        @Generated
        public Object getSimilarPlayers() {
            return this.similarPlayers;
        }

        @Generated
        public void setPlayerId(Long playerId) {
            this.playerId = playerId;
        }

        @Generated
        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        @Generated
        public void setPlayerSlug(String playerSlug) {
            this.playerSlug = playerSlug;
        }

        @Generated
        public void setPosition(String position) {
            this.position = position;
        }

        @Generated
        public void setShirtNumber(Integer shirtNumber) {
            this.shirtNumber = shirtNumber;
        }

        @Generated
        public void setMembershipType(String membershipType) {
            this.membershipType = membershipType;
        }

        @Generated
        public void setIsJokerCandidate(Boolean isJokerCandidate) {
            this.isJokerCandidate = isJokerCandidate;
        }

        @Generated
        public void setOverallRating(BigDecimal overallRating) {
            this.overallRating = overallRating;
        }

        @Generated
        public void setArchetype(String archetype) {
            this.archetype = archetype;
        }

        @Generated
        public void setSimilarPlayers(Object similarPlayers) {
            this.similarPlayers = similarPlayers;
        }

        @Generated
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof SquadMemberDto)) {
                return false;
            }
            SquadMemberDto other = (SquadMemberDto)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Long this$playerId = this.getPlayerId();
            Long other$playerId = other.getPlayerId();
            if (this$playerId == null ? other$playerId != null : !((Object)this$playerId).equals(other$playerId)) {
                return false;
            }
            Integer this$shirtNumber = this.getShirtNumber();
            Integer other$shirtNumber = other.getShirtNumber();
            if (this$shirtNumber == null ? other$shirtNumber != null : !((Object)this$shirtNumber).equals(other$shirtNumber)) {
                return false;
            }
            Boolean this$isJokerCandidate = this.getIsJokerCandidate();
            Boolean other$isJokerCandidate = other.getIsJokerCandidate();
            if (this$isJokerCandidate == null ? other$isJokerCandidate != null : !((Object)this$isJokerCandidate).equals(other$isJokerCandidate)) {
                return false;
            }
            String this$playerName = this.getPlayerName();
            String other$playerName = other.getPlayerName();
            if (this$playerName == null ? other$playerName != null : !this$playerName.equals(other$playerName)) {
                return false;
            }
            String this$playerSlug = this.getPlayerSlug();
            String other$playerSlug = other.getPlayerSlug();
            if (this$playerSlug == null ? other$playerSlug != null : !this$playerSlug.equals(other$playerSlug)) {
                return false;
            }
            String this$position = this.getPosition();
            String other$position = other.getPosition();
            if (this$position == null ? other$position != null : !this$position.equals(other$position)) {
                return false;
            }
            String this$membershipType = this.getMembershipType();
            String other$membershipType = other.getMembershipType();
            if (this$membershipType == null ? other$membershipType != null : !this$membershipType.equals(other$membershipType)) {
                return false;
            }
            BigDecimal this$overallRating = this.getOverallRating();
            BigDecimal other$overallRating = other.getOverallRating();
            if (this$overallRating == null ? other$overallRating != null : !((Object)this$overallRating).equals(other$overallRating)) {
                return false;
            }
            String this$archetype = this.getArchetype();
            String other$archetype = other.getArchetype();
            if (this$archetype == null ? other$archetype != null : !this$archetype.equals(other$archetype)) {
                return false;
            }
            Object this$similarPlayers = this.getSimilarPlayers();
            Object other$similarPlayers = other.getSimilarPlayers();
            return !(this$similarPlayers == null ? other$similarPlayers != null : !this$similarPlayers.equals(other$similarPlayers));
        }

        @Generated
        protected boolean canEqual(Object other) {
            return other instanceof SquadMemberDto;
        }

        @Generated
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Long $playerId = this.getPlayerId();
            result = result * 59 + ($playerId == null ? 43 : ((Object)$playerId).hashCode());
            Integer $shirtNumber = this.getShirtNumber();
            result = result * 59 + ($shirtNumber == null ? 43 : ((Object)$shirtNumber).hashCode());
            Boolean $isJokerCandidate = this.getIsJokerCandidate();
            result = result * 59 + ($isJokerCandidate == null ? 43 : ((Object)$isJokerCandidate).hashCode());
            String $playerName = this.getPlayerName();
            result = result * 59 + ($playerName == null ? 43 : $playerName.hashCode());
            String $playerSlug = this.getPlayerSlug();
            result = result * 59 + ($playerSlug == null ? 43 : $playerSlug.hashCode());
            String $position = this.getPosition();
            result = result * 59 + ($position == null ? 43 : $position.hashCode());
            String $membershipType = this.getMembershipType();
            result = result * 59 + ($membershipType == null ? 43 : $membershipType.hashCode());
            BigDecimal $overallRating = this.getOverallRating();
            result = result * 59 + ($overallRating == null ? 43 : ((Object)$overallRating).hashCode());
            String $archetype = this.getArchetype();
            result = result * 59 + ($archetype == null ? 43 : $archetype.hashCode());
            Object $similarPlayers = this.getSimilarPlayers();
            result = result * 59 + ($similarPlayers == null ? 43 : $similarPlayers.hashCode());
            return result;
        }

        @Generated
        public String toString() {
            return "TeamProfileDto.SquadMemberDto(playerId=" + this.getPlayerId() + ", playerName=" + this.getPlayerName() + ", playerSlug=" + this.getPlayerSlug() + ", position=" + this.getPosition() + ", shirtNumber=" + this.getShirtNumber() + ", membershipType=" + this.getMembershipType() + ", isJokerCandidate=" + this.getIsJokerCandidate() + ", overallRating=" + String.valueOf(this.getOverallRating()) + ", archetype=" + this.getArchetype() + ", similarPlayers=" + String.valueOf(this.getSimilarPlayers()) + ")";
        }

        @Generated
        public SquadMemberDto() {
        }

        @Generated
        public SquadMemberDto(Long playerId, String playerName, String playerSlug, String position, Integer shirtNumber, String membershipType, Boolean isJokerCandidate, BigDecimal overallRating, String archetype, Object similarPlayers) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.playerSlug = playerSlug;
            this.position = position;
            this.shirtNumber = shirtNumber;
            this.membershipType = membershipType;
            this.isJokerCandidate = isJokerCandidate;
            this.overallRating = overallRating;
            this.archetype = archetype;
            this.similarPlayers = similarPlayers;
        }

        @Generated
        public static class SquadMemberDtoBuilder {
            @Generated
            private Long playerId;
            @Generated
            private String playerName;
            @Generated
            private String playerSlug;
            @Generated
            private String position;
            @Generated
            private Integer shirtNumber;
            @Generated
            private String membershipType;
            @Generated
            private Boolean isJokerCandidate;
            @Generated
            private BigDecimal overallRating;
            @Generated
            private String archetype;
            @Generated
            private Object similarPlayers;

            @Generated
            SquadMemberDtoBuilder() {
            }

            @Generated
            public SquadMemberDtoBuilder playerId(Long playerId) {
                this.playerId = playerId;
                return this;
            }

            @Generated
            public SquadMemberDtoBuilder playerName(String playerName) {
                this.playerName = playerName;
                return this;
            }

            @Generated
            public SquadMemberDtoBuilder playerSlug(String playerSlug) {
                this.playerSlug = playerSlug;
                return this;
            }

            @Generated
            public SquadMemberDtoBuilder position(String position) {
                this.position = position;
                return this;
            }

            @Generated
            public SquadMemberDtoBuilder shirtNumber(Integer shirtNumber) {
                this.shirtNumber = shirtNumber;
                return this;
            }

            @Generated
            public SquadMemberDtoBuilder membershipType(String membershipType) {
                this.membershipType = membershipType;
                return this;
            }

            @Generated
            public SquadMemberDtoBuilder isJokerCandidate(Boolean isJokerCandidate) {
                this.isJokerCandidate = isJokerCandidate;
                return this;
            }

            @Generated
            public SquadMemberDtoBuilder overallRating(BigDecimal overallRating) {
                this.overallRating = overallRating;
                return this;
            }

            @Generated
            public SquadMemberDtoBuilder archetype(String archetype) {
                this.archetype = archetype;
                return this;
            }

            @Generated
            public SquadMemberDtoBuilder similarPlayers(Object similarPlayers) {
                this.similarPlayers = similarPlayers;
                return this;
            }

            @Generated
            public SquadMemberDto build() {
                return new SquadMemberDto(this.playerId, this.playerName, this.playerSlug, this.position, this.shirtNumber, this.membershipType, this.isJokerCandidate, this.overallRating, this.archetype, this.similarPlayers);
            }

            @Generated
            public String toString() {
                return "TeamProfileDto.SquadMemberDto.SquadMemberDtoBuilder(playerId=" + this.playerId + ", playerName=" + this.playerName + ", playerSlug=" + this.playerSlug + ", position=" + this.position + ", shirtNumber=" + this.shirtNumber + ", membershipType=" + this.membershipType + ", isJokerCandidate=" + this.isJokerCandidate + ", overallRating=" + String.valueOf(this.overallRating) + ", archetype=" + this.archetype + ", similarPlayers=" + String.valueOf(this.similarPlayers) + ")";
            }
        }
    }
}

