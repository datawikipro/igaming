/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  pro.datawiki.igaming.dto.CanonicalMatchHeaderDto
 */
package pro.datawiki.igaming.mdm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;

public class IngestionBlueprintDto
implements Serializable {
    private String externalSystemCode;
    private Map<String, CanonicalSportDto> sports;
    private Map<String, CanonicalCountryDto> countries;
    private Map<String, CanonicalTournamentDto> tournaments;
    private Map<String, CanonicalTeamDto> teams;
    private List<CanonicalMatchHeaderDto> activeMatches;

    @Generated
    private static Map<String, CanonicalSportDto> $default$sports() {
        return new HashMap<String, CanonicalSportDto>();
    }

    @Generated
    private static Map<String, CanonicalCountryDto> $default$countries() {
        return new HashMap<String, CanonicalCountryDto>();
    }

    @Generated
    private static Map<String, CanonicalTournamentDto> $default$tournaments() {
        return new HashMap<String, CanonicalTournamentDto>();
    }

    @Generated
    private static Map<String, CanonicalTeamDto> $default$teams() {
        return new HashMap<String, CanonicalTeamDto>();
    }

    @Generated
    private static List<CanonicalMatchHeaderDto> $default$activeMatches() {
        return new ArrayList<CanonicalMatchHeaderDto>();
    }

    @Generated
    public static IngestionBlueprintDtoBuilder builder() {
        return new IngestionBlueprintDtoBuilder();
    }

    @Generated
    public String getExternalSystemCode() {
        return this.externalSystemCode;
    }

    @Generated
    public Map<String, CanonicalSportDto> getSports() {
        return this.sports;
    }

    @Generated
    public Map<String, CanonicalCountryDto> getCountries() {
        return this.countries;
    }

    @Generated
    public Map<String, CanonicalTournamentDto> getTournaments() {
        return this.tournaments;
    }

    @Generated
    public Map<String, CanonicalTeamDto> getTeams() {
        return this.teams;
    }

    @Generated
    public List<CanonicalMatchHeaderDto> getActiveMatches() {
        return this.activeMatches;
    }

    @Generated
    public void setExternalSystemCode(String externalSystemCode) {
        this.externalSystemCode = externalSystemCode;
    }

    @Generated
    public void setSports(Map<String, CanonicalSportDto> sports) {
        this.sports = sports;
    }

    @Generated
    public void setCountries(Map<String, CanonicalCountryDto> countries) {
        this.countries = countries;
    }

    @Generated
    public void setTournaments(Map<String, CanonicalTournamentDto> tournaments) {
        this.tournaments = tournaments;
    }

    @Generated
    public void setTeams(Map<String, CanonicalTeamDto> teams) {
        this.teams = teams;
    }

    @Generated
    public void setActiveMatches(List<CanonicalMatchHeaderDto> activeMatches) {
        this.activeMatches = activeMatches;
    }

    @Generated
    public IngestionBlueprintDto() {
        this.sports = IngestionBlueprintDto.$default$sports();
        this.countries = IngestionBlueprintDto.$default$countries();
        this.tournaments = IngestionBlueprintDto.$default$tournaments();
        this.teams = IngestionBlueprintDto.$default$teams();
        this.activeMatches = IngestionBlueprintDto.$default$activeMatches();
    }

    @Generated
    public IngestionBlueprintDto(String externalSystemCode, Map<String, CanonicalSportDto> sports, Map<String, CanonicalCountryDto> countries, Map<String, CanonicalTournamentDto> tournaments, Map<String, CanonicalTeamDto> teams, List<CanonicalMatchHeaderDto> activeMatches) {
        this.externalSystemCode = externalSystemCode;
        this.sports = sports;
        this.countries = countries;
        this.tournaments = tournaments;
        this.teams = teams;
        this.activeMatches = activeMatches;
    }

    @Generated
    public static class IngestionBlueprintDtoBuilder {
        @Generated
        private String externalSystemCode;
        @Generated
        private boolean sports$set;
        @Generated
        private Map<String, CanonicalSportDto> sports$value;
        @Generated
        private boolean countries$set;
        @Generated
        private Map<String, CanonicalCountryDto> countries$value;
        @Generated
        private boolean tournaments$set;
        @Generated
        private Map<String, CanonicalTournamentDto> tournaments$value;
        @Generated
        private boolean teams$set;
        @Generated
        private Map<String, CanonicalTeamDto> teams$value;
        @Generated
        private boolean activeMatches$set;
        @Generated
        private List<CanonicalMatchHeaderDto> activeMatches$value;

        @Generated
        IngestionBlueprintDtoBuilder() {
        }

        @Generated
        public IngestionBlueprintDtoBuilder externalSystemCode(String externalSystemCode) {
            this.externalSystemCode = externalSystemCode;
            return this;
        }

        @Generated
        public IngestionBlueprintDtoBuilder sports(Map<String, CanonicalSportDto> sports) {
            this.sports$value = sports;
            this.sports$set = true;
            return this;
        }

        @Generated
        public IngestionBlueprintDtoBuilder countries(Map<String, CanonicalCountryDto> countries) {
            this.countries$value = countries;
            this.countries$set = true;
            return this;
        }

        @Generated
        public IngestionBlueprintDtoBuilder tournaments(Map<String, CanonicalTournamentDto> tournaments) {
            this.tournaments$value = tournaments;
            this.tournaments$set = true;
            return this;
        }

        @Generated
        public IngestionBlueprintDtoBuilder teams(Map<String, CanonicalTeamDto> teams) {
            this.teams$value = teams;
            this.teams$set = true;
            return this;
        }

        @Generated
        public IngestionBlueprintDtoBuilder activeMatches(List<CanonicalMatchHeaderDto> activeMatches) {
            this.activeMatches$value = activeMatches;
            this.activeMatches$set = true;
            return this;
        }

        @Generated
        public IngestionBlueprintDto build() {
            Map<String, CanonicalSportDto> sports$value = this.sports$value;
            if (!this.sports$set) {
                sports$value = IngestionBlueprintDto.$default$sports();
            }
            Map<String, CanonicalCountryDto> countries$value = this.countries$value;
            if (!this.countries$set) {
                countries$value = IngestionBlueprintDto.$default$countries();
            }
            Map<String, CanonicalTournamentDto> tournaments$value = this.tournaments$value;
            if (!this.tournaments$set) {
                tournaments$value = IngestionBlueprintDto.$default$tournaments();
            }
            Map<String, CanonicalTeamDto> teams$value = this.teams$value;
            if (!this.teams$set) {
                teams$value = IngestionBlueprintDto.$default$teams();
            }
            List<CanonicalMatchHeaderDto> activeMatches$value = this.activeMatches$value;
            if (!this.activeMatches$set) {
                activeMatches$value = IngestionBlueprintDto.$default$activeMatches();
            }
            return new IngestionBlueprintDto(this.externalSystemCode, sports$value, countries$value, tournaments$value, teams$value, activeMatches$value);
        }

        @Generated
        public String toString() {
            return "IngestionBlueprintDto.IngestionBlueprintDtoBuilder(externalSystemCode=" + this.externalSystemCode + ", sports$value=" + String.valueOf(this.sports$value) + ", countries$value=" + String.valueOf(this.countries$value) + ", tournaments$value=" + String.valueOf(this.tournaments$value) + ", teams$value=" + String.valueOf(this.teams$value) + ", activeMatches$value=" + String.valueOf(this.activeMatches$value) + ")";
        }
    }

    public static class CanonicalTeamDto
    implements Serializable {
        private Long teamId;
        private Integer sportId;
        private Integer categoryId;
        private Integer countryId;
        private String countryName;
        private Long organizationId;
        private String slug;
        private Long sofascoreId;
        private String canonicalName;
        private Boolean isNational;
        private Boolean isIndividual;
        private String gender;
        private String ageCategory;

        @Generated
        public static CanonicalTeamDtoBuilder builder() {
            return new CanonicalTeamDtoBuilder();
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
        public Integer getCategoryId() {
            return this.categoryId;
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
        public Long getOrganizationId() {
            return this.organizationId;
        }

        @Generated
        public String getSlug() {
            return this.slug;
        }

        @Generated
        public Long getSofascoreId() {
            return this.sofascoreId;
        }

        @Generated
        public String getCanonicalName() {
            return this.canonicalName;
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
        public void setSportId(Integer sportId) {
            this.sportId = sportId;
        }

        @Generated
        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
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
        public void setOrganizationId(Long organizationId) {
            this.organizationId = organizationId;
        }

        @Generated
        public void setSlug(String slug) {
            this.slug = slug;
        }

        @Generated
        public void setSofascoreId(Long sofascoreId) {
            this.sofascoreId = sofascoreId;
        }

        @Generated
        public void setCanonicalName(String canonicalName) {
            this.canonicalName = canonicalName;
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
        public void setGender(String gender) {
            this.gender = gender;
        }

        @Generated
        public void setAgeCategory(String ageCategory) {
            this.ageCategory = ageCategory;
        }

        @Generated
        public CanonicalTeamDto() {
        }

        @Generated
        public CanonicalTeamDto(Long teamId, Integer sportId, Integer categoryId, Integer countryId, String countryName, Long organizationId, String slug, Long sofascoreId, String canonicalName, Boolean isNational, Boolean isIndividual, String gender, String ageCategory) {
            this.teamId = teamId;
            this.sportId = sportId;
            this.categoryId = categoryId;
            this.countryId = countryId;
            this.countryName = countryName;
            this.organizationId = organizationId;
            this.slug = slug;
            this.sofascoreId = sofascoreId;
            this.canonicalName = canonicalName;
            this.isNational = isNational;
            this.isIndividual = isIndividual;
            this.gender = gender;
            this.ageCategory = ageCategory;
        }

        @Generated
        public static class CanonicalTeamDtoBuilder {
            @Generated
            private Long teamId;
            @Generated
            private Integer sportId;
            @Generated
            private Integer categoryId;
            @Generated
            private Integer countryId;
            @Generated
            private String countryName;
            @Generated
            private Long organizationId;
            @Generated
            private String slug;
            @Generated
            private Long sofascoreId;
            @Generated
            private String canonicalName;
            @Generated
            private Boolean isNational;
            @Generated
            private Boolean isIndividual;
            @Generated
            private String gender;
            @Generated
            private String ageCategory;

            @Generated
            CanonicalTeamDtoBuilder() {
            }

            @Generated
            public CanonicalTeamDtoBuilder teamId(Long teamId) {
                this.teamId = teamId;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder sportId(Integer sportId) {
                this.sportId = sportId;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder categoryId(Integer categoryId) {
                this.categoryId = categoryId;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder countryId(Integer countryId) {
                this.countryId = countryId;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder countryName(String countryName) {
                this.countryName = countryName;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder organizationId(Long organizationId) {
                this.organizationId = organizationId;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder slug(String slug) {
                this.slug = slug;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder sofascoreId(Long sofascoreId) {
                this.sofascoreId = sofascoreId;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder canonicalName(String canonicalName) {
                this.canonicalName = canonicalName;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder isNational(Boolean isNational) {
                this.isNational = isNational;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder isIndividual(Boolean isIndividual) {
                this.isIndividual = isIndividual;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder gender(String gender) {
                this.gender = gender;
                return this;
            }

            @Generated
            public CanonicalTeamDtoBuilder ageCategory(String ageCategory) {
                this.ageCategory = ageCategory;
                return this;
            }

            @Generated
            public CanonicalTeamDto build() {
                return new CanonicalTeamDto(this.teamId, this.sportId, this.categoryId, this.countryId, this.countryName, this.organizationId, this.slug, this.sofascoreId, this.canonicalName, this.isNational, this.isIndividual, this.gender, this.ageCategory);
            }

            @Generated
            public String toString() {
                return "IngestionBlueprintDto.CanonicalTeamDto.CanonicalTeamDtoBuilder(teamId=" + this.teamId + ", sportId=" + this.sportId + ", categoryId=" + this.categoryId + ", countryId=" + this.countryId + ", countryName=" + this.countryName + ", organizationId=" + this.organizationId + ", slug=" + this.slug + ", sofascoreId=" + this.sofascoreId + ", canonicalName=" + this.canonicalName + ", isNational=" + this.isNational + ", isIndividual=" + this.isIndividual + ", gender=" + this.gender + ", ageCategory=" + this.ageCategory + ")";
            }
        }
    }

    public static class CanonicalTournamentDto
    implements Serializable {
        private Long tournamentId;
        private Integer sportId;
        private Integer categoryId;
        private Integer countryId;
        private String countryName;
        private String slug;
        private Long sofascoreId;
        private String canonicalName;
        private String gender;
        private String ageCategory;

        @Generated
        public static CanonicalTournamentDtoBuilder builder() {
            return new CanonicalTournamentDtoBuilder();
        }

        @Generated
        public Long getTournamentId() {
            return this.tournamentId;
        }

        @Generated
        public Integer getSportId() {
            return this.sportId;
        }

        @Generated
        public Integer getCategoryId() {
            return this.categoryId;
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
        public Long getSofascoreId() {
            return this.sofascoreId;
        }

        @Generated
        public String getCanonicalName() {
            return this.canonicalName;
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
        public void setSportId(Integer sportId) {
            this.sportId = sportId;
        }

        @Generated
        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
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
        public void setSofascoreId(Long sofascoreId) {
            this.sofascoreId = sofascoreId;
        }

        @Generated
        public void setCanonicalName(String canonicalName) {
            this.canonicalName = canonicalName;
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
        public CanonicalTournamentDto() {
        }

        @Generated
        public CanonicalTournamentDto(Long tournamentId, Integer sportId, Integer categoryId, Integer countryId, String countryName, String slug, Long sofascoreId, String canonicalName, String gender, String ageCategory) {
            this.tournamentId = tournamentId;
            this.sportId = sportId;
            this.categoryId = categoryId;
            this.countryId = countryId;
            this.countryName = countryName;
            this.slug = slug;
            this.sofascoreId = sofascoreId;
            this.canonicalName = canonicalName;
            this.gender = gender;
            this.ageCategory = ageCategory;
        }

        @Generated
        public static class CanonicalTournamentDtoBuilder {
            @Generated
            private Long tournamentId;
            @Generated
            private Integer sportId;
            @Generated
            private Integer categoryId;
            @Generated
            private Integer countryId;
            @Generated
            private String countryName;
            @Generated
            private String slug;
            @Generated
            private Long sofascoreId;
            @Generated
            private String canonicalName;
            @Generated
            private String gender;
            @Generated
            private String ageCategory;

            @Generated
            CanonicalTournamentDtoBuilder() {
            }

            @Generated
            public CanonicalTournamentDtoBuilder tournamentId(Long tournamentId) {
                this.tournamentId = tournamentId;
                return this;
            }

            @Generated
            public CanonicalTournamentDtoBuilder sportId(Integer sportId) {
                this.sportId = sportId;
                return this;
            }

            @Generated
            public CanonicalTournamentDtoBuilder categoryId(Integer categoryId) {
                this.categoryId = categoryId;
                return this;
            }

            @Generated
            public CanonicalTournamentDtoBuilder countryId(Integer countryId) {
                this.countryId = countryId;
                return this;
            }

            @Generated
            public CanonicalTournamentDtoBuilder countryName(String countryName) {
                this.countryName = countryName;
                return this;
            }

            @Generated
            public CanonicalTournamentDtoBuilder slug(String slug) {
                this.slug = slug;
                return this;
            }

            @Generated
            public CanonicalTournamentDtoBuilder sofascoreId(Long sofascoreId) {
                this.sofascoreId = sofascoreId;
                return this;
            }

            @Generated
            public CanonicalTournamentDtoBuilder canonicalName(String canonicalName) {
                this.canonicalName = canonicalName;
                return this;
            }

            @Generated
            public CanonicalTournamentDtoBuilder gender(String gender) {
                this.gender = gender;
                return this;
            }

            @Generated
            public CanonicalTournamentDtoBuilder ageCategory(String ageCategory) {
                this.ageCategory = ageCategory;
                return this;
            }

            @Generated
            public CanonicalTournamentDto build() {
                return new CanonicalTournamentDto(this.tournamentId, this.sportId, this.categoryId, this.countryId, this.countryName, this.slug, this.sofascoreId, this.canonicalName, this.gender, this.ageCategory);
            }

            @Generated
            public String toString() {
                return "IngestionBlueprintDto.CanonicalTournamentDto.CanonicalTournamentDtoBuilder(tournamentId=" + this.tournamentId + ", sportId=" + this.sportId + ", categoryId=" + this.categoryId + ", countryId=" + this.countryId + ", countryName=" + this.countryName + ", slug=" + this.slug + ", sofascoreId=" + this.sofascoreId + ", canonicalName=" + this.canonicalName + ", gender=" + this.gender + ", ageCategory=" + this.ageCategory + ")";
            }
        }
    }

    public static class CanonicalSportDto
    implements Serializable {
        private Integer sportId;
        private String slug;
        private String domainKey;
        private Boolean isEsports;
        private Boolean isTeamSport;

        @Generated
        public static CanonicalSportDtoBuilder builder() {
            return new CanonicalSportDtoBuilder();
        }

        @Generated
        public Integer getSportId() {
            return this.sportId;
        }

        @Generated
        public String getSlug() {
            return this.slug;
        }

        @Generated
        public String getDomainKey() {
            return this.domainKey;
        }

        @Generated
        public Boolean getIsEsports() {
            return this.isEsports;
        }

        @Generated
        public Boolean getIsTeamSport() {
            return this.isTeamSport;
        }

        @Generated
        public void setSportId(Integer sportId) {
            this.sportId = sportId;
        }

        @Generated
        public void setSlug(String slug) {
            this.slug = slug;
        }

        @Generated
        public void setDomainKey(String domainKey) {
            this.domainKey = domainKey;
        }

        @Generated
        public void setIsEsports(Boolean isEsports) {
            this.isEsports = isEsports;
        }

        @Generated
        public void setIsTeamSport(Boolean isTeamSport) {
            this.isTeamSport = isTeamSport;
        }

        @Generated
        public CanonicalSportDto() {
        }

        @Generated
        public CanonicalSportDto(Integer sportId, String slug, String domainKey, Boolean isEsports, Boolean isTeamSport) {
            this.sportId = sportId;
            this.slug = slug;
            this.domainKey = domainKey;
            this.isEsports = isEsports;
            this.isTeamSport = isTeamSport;
        }

        @Generated
        public static class CanonicalSportDtoBuilder {
            @Generated
            private Integer sportId;
            @Generated
            private String slug;
            @Generated
            private String domainKey;
            @Generated
            private Boolean isEsports;
            @Generated
            private Boolean isTeamSport;

            @Generated
            CanonicalSportDtoBuilder() {
            }

            @Generated
            public CanonicalSportDtoBuilder sportId(Integer sportId) {
                this.sportId = sportId;
                return this;
            }

            @Generated
            public CanonicalSportDtoBuilder slug(String slug) {
                this.slug = slug;
                return this;
            }

            @Generated
            public CanonicalSportDtoBuilder domainKey(String domainKey) {
                this.domainKey = domainKey;
                return this;
            }

            @Generated
            public CanonicalSportDtoBuilder isEsports(Boolean isEsports) {
                this.isEsports = isEsports;
                return this;
            }

            @Generated
            public CanonicalSportDtoBuilder isTeamSport(Boolean isTeamSport) {
                this.isTeamSport = isTeamSport;
                return this;
            }

            @Generated
            public CanonicalSportDto build() {
                return new CanonicalSportDto(this.sportId, this.slug, this.domainKey, this.isEsports, this.isTeamSport);
            }

            @Generated
            public String toString() {
                return "IngestionBlueprintDto.CanonicalSportDto.CanonicalSportDtoBuilder(sportId=" + this.sportId + ", slug=" + this.slug + ", domainKey=" + this.domainKey + ", isEsports=" + this.isEsports + ", isTeamSport=" + this.isTeamSport + ")";
            }
        }
    }

    public static class CanonicalCountryDto
    implements Serializable {
        private Integer countryId;
        private String isoCode;
        private String canonicalName;
        private String flagCode;

        @Generated
        public static CanonicalCountryDtoBuilder builder() {
            return new CanonicalCountryDtoBuilder();
        }

        @Generated
        public Integer getCountryId() {
            return this.countryId;
        }

        @Generated
        public String getIsoCode() {
            return this.isoCode;
        }

        @Generated
        public String getCanonicalName() {
            return this.canonicalName;
        }

        @Generated
        public String getFlagCode() {
            return this.flagCode;
        }

        @Generated
        public void setCountryId(Integer countryId) {
            this.countryId = countryId;
        }

        @Generated
        public void setIsoCode(String isoCode) {
            this.isoCode = isoCode;
        }

        @Generated
        public void setCanonicalName(String canonicalName) {
            this.canonicalName = canonicalName;
        }

        @Generated
        public void setFlagCode(String flagCode) {
            this.flagCode = flagCode;
        }

        @Generated
        public CanonicalCountryDto() {
        }

        @Generated
        public CanonicalCountryDto(Integer countryId, String isoCode, String canonicalName, String flagCode) {
            this.countryId = countryId;
            this.isoCode = isoCode;
            this.canonicalName = canonicalName;
            this.flagCode = flagCode;
        }

        @Generated
        public static class CanonicalCountryDtoBuilder {
            @Generated
            private Integer countryId;
            @Generated
            private String isoCode;
            @Generated
            private String canonicalName;
            @Generated
            private String flagCode;

            @Generated
            CanonicalCountryDtoBuilder() {
            }

            @Generated
            public CanonicalCountryDtoBuilder countryId(Integer countryId) {
                this.countryId = countryId;
                return this;
            }

            @Generated
            public CanonicalCountryDtoBuilder isoCode(String isoCode) {
                this.isoCode = isoCode;
                return this;
            }

            @Generated
            public CanonicalCountryDtoBuilder canonicalName(String canonicalName) {
                this.canonicalName = canonicalName;
                return this;
            }

            @Generated
            public CanonicalCountryDtoBuilder flagCode(String flagCode) {
                this.flagCode = flagCode;
                return this;
            }

            @Generated
            public CanonicalCountryDto build() {
                return new CanonicalCountryDto(this.countryId, this.isoCode, this.canonicalName, this.flagCode);
            }

            @Generated
            public String toString() {
                return "IngestionBlueprintDto.CanonicalCountryDto.CanonicalCountryDtoBuilder(countryId=" + this.countryId + ", isoCode=" + this.isoCode + ", canonicalName=" + this.canonicalName + ", flagCode=" + this.flagCode + ")";
            }
        }
    }
}

