/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pro.datawiki.igaming.mdm.dto;

import java.io.Serializable;
import lombok.Generated;

public class TeamCandidateDto
implements Serializable {
    private Long teamId;
    private String name;
    private Integer sportId;
    private Integer countryId;
    private String countryName;
    private String slug;
    private String gender;
    private String ageCategory;

    @Generated
    public static TeamCandidateDtoBuilder builder() {
        return new TeamCandidateDtoBuilder();
    }

    @Generated
    public Long getTeamId() {
        return this.teamId;
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public Integer getSportId() {
        return this.sportId;
    }

    @Generated
    public Integer getCountryId() {
        return this.countryId;
    }

    @Generated
    public String getCountryName() {
        return this.countryName;
    }

    @Generated
    public String getSlug() {
        return this.slug;
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
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    @Generated
    public void setName(String name) {
        this.name = name;
    }

    @Generated
    public void setSportId(Integer sportId) {
        this.sportId = sportId;
    }

    @Generated
    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    @Generated
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @Generated
    public void setSlug(String slug) {
        this.slug = slug;
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
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TeamCandidateDto)) {
            return false;
        }
        TeamCandidateDto other = (TeamCandidateDto)o;
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
        Integer this$countryId = this.getCountryId();
        Integer other$countryId = other.getCountryId();
        if (this$countryId == null ? other$countryId != null : !((Object)this$countryId).equals(other$countryId)) {
            return false;
        }
        String this$name = this.getName();
        String other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        String this$countryName = this.getCountryName();
        String other$countryName = other.getCountryName();
        if (this$countryName == null ? other$countryName != null : !this$countryName.equals(other$countryName)) {
            return false;
        }
        String this$slug = this.getSlug();
        String other$slug = other.getSlug();
        if (this$slug == null ? other$slug != null : !this$slug.equals(other$slug)) {
            return false;
        }
        String this$gender = this.getGender();
        String other$gender = other.getGender();
        if (this$gender == null ? other$gender != null : !this$gender.equals(other$gender)) {
            return false;
        }
        String this$ageCategory = this.getAgeCategory();
        String other$ageCategory = other.getAgeCategory();
        return !(this$ageCategory == null ? other$ageCategory != null : !this$ageCategory.equals(other$ageCategory));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof TeamCandidateDto;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $teamId = this.getTeamId();
        result = result * 59 + ($teamId == null ? 43 : ((Object)$teamId).hashCode());
        Integer $sportId = this.getSportId();
        result = result * 59 + ($sportId == null ? 43 : ((Object)$sportId).hashCode());
        Integer $countryId = this.getCountryId();
        result = result * 59 + ($countryId == null ? 43 : ((Object)$countryId).hashCode());
        String $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        String $countryName = this.getCountryName();
        result = result * 59 + ($countryName == null ? 43 : $countryName.hashCode());
        String $slug = this.getSlug();
        result = result * 59 + ($slug == null ? 43 : $slug.hashCode());
        String $gender = this.getGender();
        result = result * 59 + ($gender == null ? 43 : $gender.hashCode());
        String $ageCategory = this.getAgeCategory();
        result = result * 59 + ($ageCategory == null ? 43 : $ageCategory.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "TeamCandidateDto(teamId=" + this.getTeamId() + ", name=" + this.getName() + ", sportId=" + this.getSportId() + ", countryId=" + this.getCountryId() + ", countryName=" + this.getCountryName() + ", slug=" + this.getSlug() + ", gender=" + this.getGender() + ", ageCategory=" + this.getAgeCategory() + ")";
    }

    @Generated
    public TeamCandidateDto() {
    }

    @Generated
    public TeamCandidateDto(Long teamId, String name, Integer sportId, Integer countryId, String countryName, String slug, String gender, String ageCategory) {
        this.teamId = teamId;
        this.name = name;
        this.sportId = sportId;
        this.countryId = countryId;
        this.countryName = countryName;
        this.slug = slug;
        this.gender = gender;
        this.ageCategory = ageCategory;
    }

    @Generated
    public static class TeamCandidateDtoBuilder {
        @Generated
        private Long teamId;
        @Generated
        private String name;
        @Generated
        private Integer sportId;
        @Generated
        private Integer countryId;
        @Generated
        private String countryName;
        @Generated
        private String slug;
        @Generated
        private String gender;
        @Generated
        private String ageCategory;

        @Generated
        TeamCandidateDtoBuilder() {
        }

        @Generated
        public TeamCandidateDtoBuilder teamId(Long teamId) {
            this.teamId = teamId;
            return this;
        }

        @Generated
        public TeamCandidateDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Generated
        public TeamCandidateDtoBuilder sportId(Integer sportId) {
            this.sportId = sportId;
            return this;
        }

        @Generated
        public TeamCandidateDtoBuilder countryId(Integer countryId) {
            this.countryId = countryId;
            return this;
        }

        @Generated
        public TeamCandidateDtoBuilder countryName(String countryName) {
            this.countryName = countryName;
            return this;
        }

        @Generated
        public TeamCandidateDtoBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public TeamCandidateDtoBuilder gender(String gender) {
            this.gender = gender;
            return this;
        }

        @Generated
        public TeamCandidateDtoBuilder ageCategory(String ageCategory) {
            this.ageCategory = ageCategory;
            return this;
        }

        @Generated
        public TeamCandidateDto build() {
            return new TeamCandidateDto(this.teamId, this.name, this.sportId, this.countryId, this.countryName, this.slug, this.gender, this.ageCategory);
        }

        @Generated
        public String toString() {
            return "TeamCandidateDto.TeamCandidateDtoBuilder(teamId=" + this.teamId + ", name=" + this.name + ", sportId=" + this.sportId + ", countryId=" + this.countryId + ", countryName=" + this.countryName + ", slug=" + this.slug + ", gender=" + this.gender + ", ageCategory=" + this.ageCategory + ")";
        }
    }
}

