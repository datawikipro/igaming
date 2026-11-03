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
 *  jakarta.persistence.Index
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.Table
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pro.datawiki.igaming.mdm.domain.MdmCountry;

@Entity
@Table(name="mdm_organization", indexes={@Index(name="idx_mdm_organization_slug", columnList="slug"), @Index(name="idx_mdm_organization_country", columnList="country_id")})
public class MdmOrganization {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="canonical_name", nullable=false)
    private String canonicalName;
    @Column(nullable=false, length=191)
    private String slug;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="country_id")
    private MdmCountry country;
    @Column(name="country_name", length=128)
    private String countryName;
    @Column(length=128)
    private String city;
    @Column(name="foundation_year")
    private Integer foundationYear;
    @Column(name="logo_url", length=512)
    private String logoUrl;
    @Column(name="academy_tier", nullable=false, length=32)
    private String academyTier;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static String $default$academyTier() {
        return "STANDARD";
    }

    @Generated
    public static MdmOrganizationBuilder builder() {
        return new MdmOrganizationBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
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
    public MdmCountry getCountry() {
        return this.country;
    }

    @Generated
    public String getCountryName() {
        return this.countryName;
    }

    @Generated
    public String getCity() {
        return this.city;
    }

    @Generated
    public Integer getFoundationYear() {
        return this.foundationYear;
    }

    @Generated
    public String getLogoUrl() {
        return this.logoUrl;
    }

    @Generated
    public String getAcademyTier() {
        return this.academyTier;
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
    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    @Generated
    public void setSlug(String slug) {
        this.slug = slug;
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
    public void setCity(String city) {
        this.city = city;
    }

    @Generated
    public void setFoundationYear(Integer foundationYear) {
        this.foundationYear = foundationYear;
    }

    @Generated
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    @Generated
    public void setAcademyTier(String academyTier) {
        this.academyTier = academyTier;
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
    public MdmOrganization() {
        this.academyTier = MdmOrganization.$default$academyTier();
    }

    @Generated
    public MdmOrganization(Long id, String canonicalName, String slug, MdmCountry country, String countryName, String city, Integer foundationYear, String logoUrl, String academyTier, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.canonicalName = canonicalName;
        this.slug = slug;
        this.country = country;
        this.countryName = countryName;
        this.city = city;
        this.foundationYear = foundationYear;
        this.logoUrl = logoUrl;
        this.academyTier = academyTier;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmOrganizationBuilder {
        @Generated
        private Long id;
        @Generated
        private String canonicalName;
        @Generated
        private String slug;
        @Generated
        private MdmCountry country;
        @Generated
        private String countryName;
        @Generated
        private String city;
        @Generated
        private Integer foundationYear;
        @Generated
        private String logoUrl;
        @Generated
        private boolean academyTier$set;
        @Generated
        private String academyTier$value;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmOrganizationBuilder() {
        }

        @Generated
        public MdmOrganizationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmOrganizationBuilder canonicalName(String canonicalName) {
            this.canonicalName = canonicalName;
            return this;
        }

        @Generated
        public MdmOrganizationBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public MdmOrganizationBuilder country(MdmCountry country) {
            this.country = country;
            return this;
        }

        @Generated
        public MdmOrganizationBuilder countryName(String countryName) {
            this.countryName = countryName;
            return this;
        }

        @Generated
        public MdmOrganizationBuilder city(String city) {
            this.city = city;
            return this;
        }

        @Generated
        public MdmOrganizationBuilder foundationYear(Integer foundationYear) {
            this.foundationYear = foundationYear;
            return this;
        }

        @Generated
        public MdmOrganizationBuilder logoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
            return this;
        }

        @Generated
        public MdmOrganizationBuilder academyTier(String academyTier) {
            this.academyTier$value = academyTier;
            this.academyTier$set = true;
            return this;
        }

        @Generated
        public MdmOrganizationBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmOrganizationBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmOrganization build() {
            String academyTier$value = this.academyTier$value;
            if (!this.academyTier$set) {
                academyTier$value = MdmOrganization.$default$academyTier();
            }
            return new MdmOrganization(this.id, this.canonicalName, this.slug, this.country, this.countryName, this.city, this.foundationYear, this.logoUrl, academyTier$value, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmOrganization.MdmOrganizationBuilder(id=" + this.id + ", canonicalName=" + this.canonicalName + ", slug=" + this.slug + ", country=" + String.valueOf(this.country) + ", countryName=" + this.countryName + ", city=" + this.city + ", foundationYear=" + this.foundationYear + ", logoUrl=" + this.logoUrl + ", academyTier$value=" + this.academyTier$value + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

