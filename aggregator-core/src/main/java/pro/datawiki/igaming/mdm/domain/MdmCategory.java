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
import pro.datawiki.igaming.mdm.domain.MdmSport;

@Entity
@Table(name="mdm_category", uniqueConstraints={@UniqueConstraint(name="uk_mdm_category_sport_slug", columnNames={"sport_id", "slug"})})
public class MdmCategory {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="sport_id", nullable=false)
    private MdmSport sport;
    @Column(nullable=false, length=128)
    private String slug;
    @Column(length=16)
    private String code;
    @Column(name="sofascore_id", unique=true)
    private Long sofascoreId;
    @Column(name="flag_code", length=16)
    private String flagCode;
    @Column(name="source_origin", nullable=false, length=32)
    private String sourceOrigin;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static String $default$sourceOrigin() {
        return "SOFASCORE";
    }

    @Generated
    public static MdmCategoryBuilder builder() {
        return new MdmCategoryBuilder();
    }

    @Generated
    public Integer getId() {
        return this.id;
    }

    @Generated
    public MdmSport getSport() {
        return this.sport;
    }

    @Generated
    public String getSlug() {
        return this.slug;
    }

    @Generated
    public String getCode() {
        return this.code;
    }

    @Generated
    public Long getSofascoreId() {
        return this.sofascoreId;
    }

    @Generated
    public String getFlagCode() {
        return this.flagCode;
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
    public void setId(Integer id) {
        this.id = id;
    }

    @Generated
    public void setSport(MdmSport sport) {
        this.sport = sport;
    }

    @Generated
    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Generated
    public void setCode(String code) {
        this.code = code;
    }

    @Generated
    public void setSofascoreId(Long sofascoreId) {
        this.sofascoreId = sofascoreId;
    }

    @Generated
    public void setFlagCode(String flagCode) {
        this.flagCode = flagCode;
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
    public MdmCategory() {
        this.sourceOrigin = MdmCategory.$default$sourceOrigin();
    }

    @Generated
    public MdmCategory(Integer id, MdmSport sport, String slug, String code, Long sofascoreId, String flagCode, String sourceOrigin, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.sport = sport;
        this.slug = slug;
        this.code = code;
        this.sofascoreId = sofascoreId;
        this.flagCode = flagCode;
        this.sourceOrigin = sourceOrigin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmCategoryBuilder {
        @Generated
        private Integer id;
        @Generated
        private MdmSport sport;
        @Generated
        private String slug;
        @Generated
        private String code;
        @Generated
        private Long sofascoreId;
        @Generated
        private String flagCode;
        @Generated
        private boolean sourceOrigin$set;
        @Generated
        private String sourceOrigin$value;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmCategoryBuilder() {
        }

        @Generated
        public MdmCategoryBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmCategoryBuilder sport(MdmSport sport) {
            this.sport = sport;
            return this;
        }

        @Generated
        public MdmCategoryBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public MdmCategoryBuilder code(String code) {
            this.code = code;
            return this;
        }

        @Generated
        public MdmCategoryBuilder sofascoreId(Long sofascoreId) {
            this.sofascoreId = sofascoreId;
            return this;
        }

        @Generated
        public MdmCategoryBuilder flagCode(String flagCode) {
            this.flagCode = flagCode;
            return this;
        }

        @Generated
        public MdmCategoryBuilder sourceOrigin(String sourceOrigin) {
            this.sourceOrigin$value = sourceOrigin;
            this.sourceOrigin$set = true;
            return this;
        }

        @Generated
        public MdmCategoryBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmCategoryBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmCategory build() {
            String sourceOrigin$value = this.sourceOrigin$value;
            if (!this.sourceOrigin$set) {
                sourceOrigin$value = MdmCategory.$default$sourceOrigin();
            }
            return new MdmCategory(this.id, this.sport, this.slug, this.code, this.sofascoreId, this.flagCode, sourceOrigin$value, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmCategory.MdmCategoryBuilder(id=" + this.id + ", sport=" + String.valueOf(this.sport) + ", slug=" + this.slug + ", code=" + this.code + ", sofascoreId=" + this.sofascoreId + ", flagCode=" + this.flagCode + ", sourceOrigin$value=" + this.sourceOrigin$value + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

