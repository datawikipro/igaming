/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pro.datawiki.igaming.mdm.dto;

import java.io.Serializable;
import lombok.Generated;

public class TournamentCandidateDto
implements Serializable {
    private Long tournamentId;
    private String name;
    private Integer sportId;
    private Integer countryId;
    private String countryName;
    private String slug;
    private String gender;
    private String ageCategory;

    @Generated
    public static TournamentCandidateDtoBuilder builder() {
        return new TournamentCandidateDtoBuilder();
    }

    @Generated
    public Long getTournamentId() {
        return this.tournamentId;
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
    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
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
        if (!(o instanceof TournamentCandidateDto)) {
            return false;
        }
        TournamentCandidateDto other = (TournamentCandidateDto)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$tournamentId = this.getTournamentId();
        Long other$tournamentId = other.getTournamentId();
        if (this$tournamentId == null ? other$tournamentId != null : !((Object)this$tournamentId).equals(other$tournamentId)) {
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
        return other instanceof TournamentCandidateDto;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $tournamentId = this.getTournamentId();
        result = result * 59 + ($tournamentId == null ? 43 : ((Object)$tournamentId).hashCode());
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
        return "TournamentCandidateDto(tournamentId=" + this.getTournamentId() + ", name=" + this.getName() + ", sportId=" + this.getSportId() + ", countryId=" + this.getCountryId() + ", countryName=" + this.getCountryName() + ", slug=" + this.getSlug() + ", gender=" + this.getGender() + ", ageCategory=" + this.getAgeCategory() + ")";
    }

    @Generated
    public TournamentCandidateDto() {
    }

    @Generated
    public TournamentCandidateDto(Long tournamentId, String name, Integer sportId, Integer countryId, String countryName, String slug, String gender, String ageCategory) {
        this.tournamentId = tournamentId;
        this.name = name;
        this.sportId = sportId;
        this.countryId = countryId;
        this.countryName = countryName;
        this.slug = slug;
        this.gender = gender;
        this.ageCategory = ageCategory;
    }

    @Generated
    public static class TournamentCandidateDtoBuilder {
        @Generated
        private Long tournamentId;
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
        TournamentCandidateDtoBuilder() {
        }

        @Generated
        public TournamentCandidateDtoBuilder tournamentId(Long tournamentId) {
            this.tournamentId = tournamentId;
            return this;
        }

        @Generated
        public TournamentCandidateDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Generated
        public TournamentCandidateDtoBuilder sportId(Integer sportId) {
            this.sportId = sportId;
            return this;
        }

        @Generated
        public TournamentCandidateDtoBuilder countryId(Integer countryId) {
            this.countryId = countryId;
            return this;
        }

        @Generated
        public TournamentCandidateDtoBuilder countryName(String countryName) {
            this.countryName = countryName;
            return this;
        }

        @Generated
        public TournamentCandidateDtoBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public TournamentCandidateDtoBuilder gender(String gender) {
            this.gender = gender;
            return this;
        }

        @Generated
        public TournamentCandidateDtoBuilder ageCategory(String ageCategory) {
            this.ageCategory = ageCategory;
            return this;
        }

        @Generated
        public TournamentCandidateDto build() {
            return new TournamentCandidateDto(this.tournamentId, this.name, this.sportId, this.countryId, this.countryName, this.slug, this.gender, this.ageCategory);
        }

        @Generated
        public String toString() {
            return "TournamentCandidateDto.TournamentCandidateDtoBuilder(tournamentId=" + this.tournamentId + ", name=" + this.name + ", sportId=" + this.sportId + ", countryId=" + this.countryId + ", countryName=" + this.countryName + ", slug=" + this.slug + ", gender=" + this.gender + ", ageCategory=" + this.ageCategory + ")";
        }
    }
}

