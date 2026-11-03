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
@Table(name="mdm_team", uniqueConstraints={@UniqueConstraint(name="uk_mdm_team_sport_slug", columnNames={"sport_id", "slug"})})
public class MdmTeam {
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
    @Column(name="is_national", nullable=false)
    private Boolean isNational;
    @Column(name="is_individual", nullable=false)
    private Boolean isIndividual;
    @Column(nullable=false, length=16)
    private String gender;
    @Column(name="age_category", nullable=false, length=16)
    private String ageCategory;
    @Column(name="normalized_signature", nullable=false)
    private String normalizedSignature;
    @Column(name="logo_url", length=512)
    private String logoUrl;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="country_id")
    private MdmCountry country;
    @Column(name="country_name", length=128)
    private String countryName;
    @Column(name="popularity_rank", nullable=false)
    private Integer popularityRank;
    @Column(name="source_origin", nullable=false, length=32)
    private String sourceOrigin;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static Boolean $default$isNational() {
        return false;
    }

    @Generated
    private static Boolean $default$isIndividual() {
        return false;
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
    private static Integer $default$popularityRank() {
        return 1000;
    }

    @Generated
    private static String $default$sourceOrigin() {
        return "SOFASCORE";
    }

    @Generated
    public static MdmTeamBuilder builder() {
        return new MdmTeamBuilder();
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
    public String getNormalizedSignature() {
        return this.normalizedSignature;
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
    public Integer getPopularityRank() {
        return this.popularityRank;
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
    public void setNormalizedSignature(String normalizedSignature) {
        this.normalizedSignature = normalizedSignature;
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
    public void setPopularityRank(Integer popularityRank) {
        this.popularityRank = popularityRank;
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
    public MdmTeam() {
        this.isNational = MdmTeam.$default$isNational();
        this.isIndividual = MdmTeam.$default$isIndividual();
        this.gender = MdmTeam.$default$gender();
        this.ageCategory = MdmTeam.$default$ageCategory();
        this.popularityRank = MdmTeam.$default$popularityRank();
        this.sourceOrigin = MdmTeam.$default$sourceOrigin();
    }

    @Generated
    public MdmTeam(Long id, MdmSport sport, MdmCategory category, String slug, Long sofascoreId, String canonicalName, Boolean isNational, Boolean isIndividual, String gender, String ageCategory, String normalizedSignature, String logoUrl, MdmCountry country, String countryName, Integer popularityRank, String sourceOrigin, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.sport = sport;
        this.category = category;
        this.slug = slug;
        this.sofascoreId = sofascoreId;
        this.canonicalName = canonicalName;
        this.isNational = isNational;
        this.isIndividual = isIndividual;
        this.gender = gender;
        this.ageCategory = ageCategory;
        this.normalizedSignature = normalizedSignature;
        this.logoUrl = logoUrl;
        this.country = country;
        this.countryName = countryName;
        this.popularityRank = popularityRank;
        this.sourceOrigin = sourceOrigin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmTeamBuilder {
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
        private boolean isNational$set;
        @Generated
        private Boolean isNational$value;
        @Generated
        private boolean isIndividual$set;
        @Generated
        private Boolean isIndividual$value;
        @Generated
        private boolean gender$set;
        @Generated
        private String gender$value;
        @Generated
        private boolean ageCategory$set;
        @Generated
        private String ageCategory$value;
        @Generated
        private String normalizedSignature;
        @Generated
        private String logoUrl;
        @Generated
        private MdmCountry country;
        @Generated
        private String countryName;
        @Generated
        private boolean popularityRank$set;
        @Generated
        private Integer popularityRank$value;
        @Generated
        private boolean sourceOrigin$set;
        @Generated
        private String sourceOrigin$value;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmTeamBuilder() {
        }

        @Generated
        public MdmTeamBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmTeamBuilder sport(MdmSport sport) {
            this.sport = sport;
            return this;
        }

        @Generated
        public MdmTeamBuilder category(MdmCategory category) {
            this.category = category;
            return this;
        }

        @Generated
        public MdmTeamBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public MdmTeamBuilder sofascoreId(Long sofascoreId) {
            this.sofascoreId = sofascoreId;
            return this;
        }

        @Generated
        public MdmTeamBuilder canonicalName(String canonicalName) {
            this.canonicalName = canonicalName;
            return this;
        }

        @Generated
        public MdmTeamBuilder isNational(Boolean isNational) {
            this.isNational$value = isNational;
            this.isNational$set = true;
            return this;
        }

        @Generated
        public MdmTeamBuilder isIndividual(Boolean isIndividual) {
            this.isIndividual$value = isIndividual;
            this.isIndividual$set = true;
            return this;
        }

        @Generated
        public MdmTeamBuilder gender(String gender) {
            this.gender$value = gender;
            this.gender$set = true;
            return this;
        }

        @Generated
        public MdmTeamBuilder ageCategory(String ageCategory) {
            this.ageCategory$value = ageCategory;
            this.ageCategory$set = true;
            return this;
        }

        @Generated
        public MdmTeamBuilder normalizedSignature(String normalizedSignature) {
            this.normalizedSignature = normalizedSignature;
            return this;
        }

        @Generated
        public MdmTeamBuilder logoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
            return this;
        }

        @Generated
        public MdmTeamBuilder country(MdmCountry country) {
            this.country = country;
            return this;
        }

        @Generated
        public MdmTeamBuilder countryName(String countryName) {
            this.countryName = countryName;
            return this;
        }

        @Generated
        public MdmTeamBuilder popularityRank(Integer popularityRank) {
            this.popularityRank$value = popularityRank;
            this.popularityRank$set = true;
            return this;
        }

        @Generated
        public MdmTeamBuilder sourceOrigin(String sourceOrigin) {
            this.sourceOrigin$value = sourceOrigin;
            this.sourceOrigin$set = true;
            return this;
        }

        @Generated
        public MdmTeamBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmTeamBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmTeam build() {
            Boolean isNational$value = this.isNational$value;
            if (!this.isNational$set) {
                isNational$value = MdmTeam.$default$isNational();
            }
            Boolean isIndividual$value = this.isIndividual$value;
            if (!this.isIndividual$set) {
                isIndividual$value = MdmTeam.$default$isIndividual();
            }
            String gender$value = this.gender$value;
            if (!this.gender$set) {
                gender$value = MdmTeam.$default$gender();
            }
            String ageCategory$value = this.ageCategory$value;
            if (!this.ageCategory$set) {
                ageCategory$value = MdmTeam.$default$ageCategory();
            }
            Integer popularityRank$value = this.popularityRank$value;
            if (!this.popularityRank$set) {
                popularityRank$value = MdmTeam.$default$popularityRank();
            }
            String sourceOrigin$value = this.sourceOrigin$value;
            if (!this.sourceOrigin$set) {
                sourceOrigin$value = MdmTeam.$default$sourceOrigin();
            }
            return new MdmTeam(this.id, this.sport, this.category, this.slug, this.sofascoreId, this.canonicalName, isNational$value, isIndividual$value, gender$value, ageCategory$value, this.normalizedSignature, this.logoUrl, this.country, this.countryName, popularityRank$value, sourceOrigin$value, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmTeam.MdmTeamBuilder(id=" + this.id + ", sport=" + String.valueOf(this.sport) + ", category=" + String.valueOf(this.category) + ", slug=" + this.slug + ", sofascoreId=" + this.sofascoreId + ", canonicalName=" + this.canonicalName + ", isNational$value=" + this.isNational$value + ", isIndividual$value=" + this.isIndividual$value + ", gender$value=" + this.gender$value + ", ageCategory$value=" + this.ageCategory$value + ", normalizedSignature=" + this.normalizedSignature + ", logoUrl=" + this.logoUrl + ", country=" + String.valueOf(this.country) + ", countryName=" + this.countryName + ", popularityRank$value=" + this.popularityRank$value + ", sourceOrigin$value=" + this.sourceOrigin$value + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

