/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Table
 *  jakarta.persistence.UniqueConstraint
 *  lombok.Generated
 *  org.hibernate.annotations.CreationTimestamp
 *  org.hibernate.annotations.UpdateTimestamp
 */
package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="mdm_country", uniqueConstraints={@UniqueConstraint(name="uk_mdm_country_name", columnNames={"canonical_name"})})
public class MdmCountry {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Column(name="iso_code", unique=true, length=4)
    private String isoCode;
    @Column(name="canonical_name", nullable=false, length=128)
    private String canonicalName;
    @Column(name="flag_code", length=16)
    private String flagCode;
    @Column(name="sofascore_id", unique=true)
    private Integer sofascoreId;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    public static MdmCountryBuilder builder() {
        return new MdmCountryBuilder();
    }

    @Generated
    public Integer getId() {
        return this.id;
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
    public Integer getSofascoreId() {
        return this.sofascoreId;
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
    public void setSofascoreId(Integer sofascoreId) {
        this.sofascoreId = sofascoreId;
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
    public MdmCountry() {
    }

    @Generated
    public MdmCountry(Integer id, String isoCode, String canonicalName, String flagCode, Integer sofascoreId, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.isoCode = isoCode;
        this.canonicalName = canonicalName;
        this.flagCode = flagCode;
        this.sofascoreId = sofascoreId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmCountryBuilder {
        @Generated
        private Integer id;
        @Generated
        private String isoCode;
        @Generated
        private String canonicalName;
        @Generated
        private String flagCode;
        @Generated
        private Integer sofascoreId;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmCountryBuilder() {
        }

        @Generated
        public MdmCountryBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmCountryBuilder isoCode(String isoCode) {
            this.isoCode = isoCode;
            return this;
        }

        @Generated
        public MdmCountryBuilder canonicalName(String canonicalName) {
            this.canonicalName = canonicalName;
            return this;
        }

        @Generated
        public MdmCountryBuilder flagCode(String flagCode) {
            this.flagCode = flagCode;
            return this;
        }

        @Generated
        public MdmCountryBuilder sofascoreId(Integer sofascoreId) {
            this.sofascoreId = sofascoreId;
            return this;
        }

        @Generated
        public MdmCountryBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmCountryBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmCountry build() {
            return new MdmCountry(this.id, this.isoCode, this.canonicalName, this.flagCode, this.sofascoreId, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmCountry.MdmCountryBuilder(id=" + this.id + ", isoCode=" + this.isoCode + ", canonicalName=" + this.canonicalName + ", flagCode=" + this.flagCode + ", sofascoreId=" + this.sofascoreId + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

