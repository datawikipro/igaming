/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.Table
 *  jakarta.persistence.UniqueConstraint
 *  lombok.Generated
 *  org.hibernate.annotations.CreationTimestamp
 *  org.hibernate.annotations.UpdateTimestamp
 */
package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pro.datawiki.igaming.mdm.domain.MdmCategory;
import pro.datawiki.igaming.mdm.domain.MdmCountry;
import pro.datawiki.igaming.mdm.domain.MdmSport;

@Entity
@Table(name="mdm_tournament", uniqueConstraints={@UniqueConstraint(name="uk_mdm_tournament_sport_cat_slug", columnNames={"sport_id", "category_id", "slug"})})
public class MdmTournament {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="sport_id", nullable=false)
    private MdmSport sport;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="category_id")
    private MdmCategory category;
    @Column(nullable=false, length=191)
    private String slug;
    @Column(name="sofascore_id", unique=true)
    private Long sofascoreId;
    @Column(name="canonical_name", nullable=false)
    private String canonicalName;
    @Column(nullable=false)
    private Integer tier;
    @Column(nullable=false, length=16)
    private String gender;
    @Column(name="age_category", nullable=false, length=16)
    private String ageCategory;
    @Column(name="is_cup", nullable=false)
    private Boolean isCup;
    @Column(name="popularity_rank", nullable=false)
    private Integer popularityRank;
    @Column(name="is_featured", nullable=false)
    private Boolean isFeatured;
    @Column(name="ml_eligible", nullable=false)
    private Boolean mlEligible;
    @Column(name="has_official_reports", nullable=false)
    private Boolean hasOfficialReports;
    @Column(name="has_player_stats", nullable=false)
    private Boolean hasPlayerStats;
    @Column(name="coverage_tier", nullable=false, length=32)
    private String coverageTier;
    @Column(name="logo_url", length=512)
    private String logoUrl;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="country_id")
    private MdmCountry country;
    @Column(name="country_name", length=128)
    private String countryName;
    @Column(name="source_origin", nullable=false, length=32)
    private String sourceOrigin;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static Integer $default$tier() {
        return 3;
    }

    @Generated
    private static String $default$gender() {
        return "MALE";
    }

    @Generated
    private static String $default$ageCategory() {
        return "SENIOR";
    }

    @Generated
    private static Boolean $default$isCup() {
        return false;
    }

    @Generated
    private static Integer $default$popularityRank() {
        return 1000;
    }

    @Generated
    private static Boolean $default$isFeatured() {
        return false;
    }

    @Generated
    private static Boolean $default$mlEligible() {
        return false;
    }

    @Generated
    private static Boolean $default$hasOfficialReports() {
        return false;
    }

    @Generated
    private static Boolean $default$hasPlayerStats() {
        return false;
    }

    @Generated
    private static String $default$coverageTier() {
        return "TIER_0_SCRATCH";
    }

    @Generated
    private static String $default$sourceOrigin() {
        return "SOFASCORE";
    }

    @Generated
    public static MdmTournamentBuilder builder() {
        return new MdmTournamentBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public MdmSport getSport() {
        return this.sport;
    }

    @Generated
    public MdmCategory getCategory() {
        return this.category;
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
    public Integer getTier() {
        return this.tier;
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
    public Boolean getIsCup() {
        return this.isCup;
    }

    @Generated
    public Integer getPopularityRank() {
        return this.popularityRank;
    }

    @Generated
    public Boolean getIsFeatured() {
        return this.isFeatured;
    }

    @Generated
    public Boolean getMlEligible() {
        return this.mlEligible;
    }

    @Generated
    public Boolean getHasOfficialReports() {
        return this.hasOfficialReports;
    }

    @Generated
    public Boolean getHasPlayerStats() {
        return this.hasPlayerStats;
    }

    @Generated
    public String getCoverageTier() {
        return this.coverageTier;
    }

    @Generated
    public String getLogoUrl() {
        return this.logoUrl;
    }

    @Generated
    public MdmCountry getCountry() {
        return this.country;
    }

    @Generated
    public String getCountryName() {
        return this.countryName;
    }

    @Generated
    public String getSourceOrigin() {
        return this.sourceOrigin;
    }

    @Generated
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @Generated
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @Generated
    public void setId(Long id) {
        this.id = id;
    }

    @Generated
    public void setSport(MdmSport sport) {
        this.sport = sport;
    }

    @Generated
    public void setCategory(MdmCategory category) {
        this.category = category;
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
    public void setTier(Integer tier) {
        this.tier = tier;
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
    public void setIsCup(Boolean isCup) {
        this.isCup = isCup;
    }

    @Generated
    public void setPopularityRank(Integer popularityRank) {
        this.popularityRank = popularityRank;
    }

    @Generated
    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    @Generated
    public void setMlEligible(Boolean mlEligible) {
        this.mlEligible = mlEligible;
    }

    @Generated
    public void setHasOfficialReports(Boolean hasOfficialReports) {
        this.hasOfficialReports = hasOfficialReports;
    }

    @Generated
    public void setHasPlayerStats(Boolean hasPlayerStats) {
        this.hasPlayerStats = hasPlayerStats;
    }

    @Generated
    public void setCoverageTier(String coverageTier) {
        this.coverageTier = coverageTier;
    }

    @Generated
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    @Generated
    public void setCountry(MdmCountry country) {
        this.country = country;
    }

    @Generated
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @Generated
    public void setSourceOrigin(String sourceOrigin) {
        this.sourceOrigin = sourceOrigin;
    }

    @Generated
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Generated
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Generated
    public MdmTournament() {
        this.tier = MdmTournament.$default$tier();
        this.gender = MdmTournament.$default$gender();
        this.ageCategory = MdmTournament.$default$ageCategory();
        this.isCup = MdmTournament.$default$isCup();
        this.popularityRank = MdmTournament.$default$popularityRank();
        this.isFeatured = MdmTournament.$default$isFeatured();
        this.mlEligible = MdmTournament.$default$mlEligible();
        this.hasOfficialReports = MdmTournament.$default$hasOfficialReports();
        this.hasPlayerStats = MdmTournament.$default$hasPlayerStats();
        this.coverageTier = MdmTournament.$default$coverageTier();
        this.sourceOrigin = MdmTournament.$default$sourceOrigin();
    }

    @Generated
    public MdmTournament(Long id, MdmSport sport, MdmCategory category, String slug, Long sofascoreId, String canonicalName, Integer tier, String gender, String ageCategory, Boolean isCup, Integer popularityRank, Boolean isFeatured, Boolean mlEligible, Boolean hasOfficialReports, Boolean hasPlayerStats, String coverageTier, String logoUrl, MdmCountry country, String countryName, String sourceOrigin, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.sport = sport;
        this.category = category;
        this.slug = slug;
        this.sofascoreId = sofascoreId;
        this.canonicalName = canonicalName;
        this.tier = tier;
        this.gender = gender;
        this.ageCategory = ageCategory;
        this.isCup = isCup;
        this.popularityRank = popularityRank;
        this.isFeatured = isFeatured;
        this.mlEligible = mlEligible;
        this.hasOfficialReports = hasOfficialReports;
        this.hasPlayerStats = hasPlayerStats;
        this.coverageTier = coverageTier;
        this.logoUrl = logoUrl;
        this.country = country;
        this.countryName = countryName;
        this.sourceOrigin = sourceOrigin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmTournamentBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmSport sport;
        @Generated
        private MdmCategory category;
        @Generated
        private String slug;
        @Generated
        private Long sofascoreId;
        @Generated
        private String canonicalName;
        @Generated
        private boolean tier$set;
        @Generated
        private Integer tier$value;
        @Generated
        private boolean gender$set;
        @Generated
        private String gender$value;
        @Generated
        private boolean ageCategory$set;
        @Generated
        private String ageCategory$value;
        @Generated
        private boolean isCup$set;
        @Generated
        private Boolean isCup$value;
        @Generated
        private boolean popularityRank$set;
        @Generated
        private Integer popularityRank$value;
        @Generated
        private boolean isFeatured$set;
        @Generated
        private Boolean isFeatured$value;
        @Generated
        private boolean mlEligible$set;
        @Generated
        private Boolean mlEligible$value;
        @Generated
        private boolean hasOfficialReports$set;
        @Generated
        private Boolean hasOfficialReports$value;
        @Generated
        private boolean hasPlayerStats$set;
        @Generated
        private Boolean hasPlayerStats$value;
        @Generated
        private boolean coverageTier$set;
        @Generated
        private String coverageTier$value;
        @Generated
        private String logoUrl;
        @Generated
        private MdmCountry country;
        @Generated
        private String countryName;
        @Generated
        private boolean sourceOrigin$set;
        @Generated
        private String sourceOrigin$value;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmTournamentBuilder() {
        }

        @Generated
        public MdmTournamentBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmTournamentBuilder sport(MdmSport sport) {
            this.sport = sport;
            return this;
        }

        @Generated
        public MdmTournamentBuilder category(MdmCategory category) {
            this.category = category;
            return this;
        }

        @Generated
        public MdmTournamentBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public MdmTournamentBuilder sofascoreId(Long sofascoreId) {
            this.sofascoreId = sofascoreId;
            return this;
        }

        @Generated
        public MdmTournamentBuilder canonicalName(String canonicalName) {
            this.canonicalName = canonicalName;
            return this;
        }

        @Generated
        public MdmTournamentBuilder tier(Integer tier) {
            this.tier$value = tier;
            this.tier$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder gender(String gender) {
            this.gender$value = gender;
            this.gender$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder ageCategory(String ageCategory) {
            this.ageCategory$value = ageCategory;
            this.ageCategory$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder isCup(Boolean isCup) {
            this.isCup$value = isCup;
            this.isCup$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder popularityRank(Integer popularityRank) {
            this.popularityRank$value = popularityRank;
            this.popularityRank$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder isFeatured(Boolean isFeatured) {
            this.isFeatured$value = isFeatured;
            this.isFeatured$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder mlEligible(Boolean mlEligible) {
            this.mlEligible$value = mlEligible;
            this.mlEligible$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder hasOfficialReports(Boolean hasOfficialReports) {
            this.hasOfficialReports$value = hasOfficialReports;
            this.hasOfficialReports$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder hasPlayerStats(Boolean hasPlayerStats) {
            this.hasPlayerStats$value = hasPlayerStats;
            this.hasPlayerStats$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder coverageTier(String coverageTier) {
            this.coverageTier$value = coverageTier;
            this.coverageTier$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder logoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
            return this;
        }

        @Generated
        public MdmTournamentBuilder country(MdmCountry country) {
            this.country = country;
            return this;
        }

        @Generated
        public MdmTournamentBuilder countryName(String countryName) {
            this.countryName = countryName;
            return this;
        }

        @Generated
        public MdmTournamentBuilder sourceOrigin(String sourceOrigin) {
            this.sourceOrigin$value = sourceOrigin;
            this.sourceOrigin$set = true;
            return this;
        }

        @Generated
        public MdmTournamentBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmTournamentBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmTournament build() {
            Integer tier$value = this.tier$value;
            if (!this.tier$set) {
                tier$value = MdmTournament.$default$tier();
            }
            String gender$value = this.gender$value;
            if (!this.gender$set) {
                gender$value = MdmTournament.$default$gender();
            }
            String ageCategory$value = this.ageCategory$value;
            if (!this.ageCategory$set) {
                ageCategory$value = MdmTournament.$default$ageCategory();
            }
            Boolean isCup$value = this.isCup$value;
            if (!this.isCup$set) {
                isCup$value = MdmTournament.$default$isCup();
            }
            Integer popularityRank$value = this.popularityRank$value;
            if (!this.popularityRank$set) {
                popularityRank$value = MdmTournament.$default$popularityRank();
            }
            Boolean isFeatured$value = this.isFeatured$value;
            if (!this.isFeatured$set) {
                isFeatured$value = MdmTournament.$default$isFeatured();
            }
            Boolean mlEligible$value = this.mlEligible$value;
            if (!this.mlEligible$set) {
                mlEligible$value = MdmTournament.$default$mlEligible();
            }
            Boolean hasOfficialReports$value = this.hasOfficialReports$value;
            if (!this.hasOfficialReports$set) {
                hasOfficialReports$value = MdmTournament.$default$hasOfficialReports();
            }
            Boolean hasPlayerStats$value = this.hasPlayerStats$value;
            if (!this.hasPlayerStats$set) {
                hasPlayerStats$value = MdmTournament.$default$hasPlayerStats();
            }
            String coverageTier$value = this.coverageTier$value;
            if (!this.coverageTier$set) {
                coverageTier$value = MdmTournament.$default$coverageTier();
            }
            String sourceOrigin$value = this.sourceOrigin$value;
            if (!this.sourceOrigin$set) {
                sourceOrigin$value = MdmTournament.$default$sourceOrigin();
            }
            return new MdmTournament(this.id, this.sport, this.category, this.slug, this.sofascoreId, this.canonicalName, tier$value, gender$value, ageCategory$value, isCup$value, popularityRank$value, isFeatured$value, mlEligible$value, hasOfficialReports$value, hasPlayerStats$value, coverageTier$value, this.logoUrl, this.country, this.countryName, sourceOrigin$value, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmTournament.MdmTournamentBuilder(id=" + this.id + ", sport=" + String.valueOf(this.sport) + ", category=" + String.valueOf(this.category) + ", slug=" + this.slug + ", sofascoreId=" + this.sofascoreId + ", canonicalName=" + this.canonicalName + ", tier$value=" + this.tier$value + ", gender$value=" + this.gender$value + ", ageCategory$value=" + this.ageCategory$value + ", isCup$value=" + this.isCup$value + ", popularityRank$value=" + this.popularityRank$value + ", isFeatured$value=" + this.isFeatured$value + ", mlEligible$value=" + this.mlEligible$value + ", hasOfficialReports$value=" + this.hasOfficialReports$value + ", hasPlayerStats$value=" + this.hasPlayerStats$value + ", coverageTier$value=" + this.coverageTier$value + ", logoUrl=" + this.logoUrl + ", country=" + String.valueOf(this.country) + ", countryName=" + this.countryName + ", sourceOrigin$value=" + this.sourceOrigin$value + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

