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
 *  jakarta.persistence.UniqueConstraint
 *  lombok.Generated
 *  org.hibernate.annotations.CreationTimestamp
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
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;
import pro.datawiki.igaming.mdm.domain.MdmCountry;
import pro.datawiki.igaming.mdm.domain.MdmExternalSystem;

@Entity
@Table(name="mdm_country_ingestion_map", uniqueConstraints={@UniqueConstraint(name="uk_mdm_country_map", columnNames={"external_system_id", "raw_key"})}, indexes={@Index(name="idx_mdm_country_map_lookup", columnList="external_system_id, raw_key"), @Index(name="idx_mdm_country_map_country", columnList="canonical_country_id")})
public class MdmCountryIngestionMap {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="external_system_id", nullable=false)
    private MdmExternalSystem externalSystem;
    @Column(name="raw_key", nullable=false, length=255)
    private String rawKey;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="canonical_country_id", nullable=false)
    private MdmCountry canonicalCountry;
    @Column(name="raw_name", length=255)
    private String rawName;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Generated
    public static MdmCountryIngestionMapBuilder builder() {
        return new MdmCountryIngestionMapBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public MdmExternalSystem getExternalSystem() {
        return this.externalSystem;
    }

    @Generated
    public String getRawKey() {
        return this.rawKey;
    }

    @Generated
    public MdmCountry getCanonicalCountry() {
        return this.canonicalCountry;
    }

    @Generated
    public String getRawName() {
        return this.rawName;
    }

    @Generated
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @Generated
    public void setId(Long id) {
        this.id = id;
    }

    @Generated
    public void setExternalSystem(MdmExternalSystem externalSystem) {
        this.externalSystem = externalSystem;
    }

    @Generated
    public void setRawKey(String rawKey) {
        this.rawKey = rawKey;
    }

    @Generated
    public void setCanonicalCountry(MdmCountry canonicalCountry) {
        this.canonicalCountry = canonicalCountry;
    }

    @Generated
    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    @Generated
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Generated
    public MdmCountryIngestionMap() {
    }

    @Generated
    public MdmCountryIngestionMap(Long id, MdmExternalSystem externalSystem, String rawKey, MdmCountry canonicalCountry, String rawName, OffsetDateTime createdAt) {
        this.id = id;
        this.externalSystem = externalSystem;
        this.rawKey = rawKey;
        this.canonicalCountry = canonicalCountry;
        this.rawName = rawName;
        this.createdAt = createdAt;
    }

    @Generated
    public static class MdmCountryIngestionMapBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmExternalSystem externalSystem;
        @Generated
        private String rawKey;
        @Generated
        private MdmCountry canonicalCountry;
        @Generated
        private String rawName;
        @Generated
        private OffsetDateTime createdAt;

        @Generated
        MdmCountryIngestionMapBuilder() {
        }

        @Generated
        public MdmCountryIngestionMapBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmCountryIngestionMapBuilder externalSystem(MdmExternalSystem externalSystem) {
            this.externalSystem = externalSystem;
            return this;
        }

        @Generated
        public MdmCountryIngestionMapBuilder rawKey(String rawKey) {
            this.rawKey = rawKey;
            return this;
        }

        @Generated
        public MdmCountryIngestionMapBuilder canonicalCountry(MdmCountry canonicalCountry) {
            this.canonicalCountry = canonicalCountry;
            return this;
        }

        @Generated
        public MdmCountryIngestionMapBuilder rawName(String rawName) {
            this.rawName = rawName;
            return this;
        }

        @Generated
        public MdmCountryIngestionMapBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmCountryIngestionMap build() {
            return new MdmCountryIngestionMap(this.id, this.externalSystem, this.rawKey, this.canonicalCountry, this.rawName, this.createdAt);
        }

        @Generated
        public String toString() {
            return "MdmCountryIngestionMap.MdmCountryIngestionMapBuilder(id=" + this.id + ", externalSystem=" + String.valueOf(this.externalSystem) + ", rawKey=" + this.rawKey + ", canonicalCountry=" + String.valueOf(this.canonicalCountry) + ", rawName=" + this.rawName + ", createdAt=" + String.valueOf(this.createdAt) + ")";
        }
    }
}

