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
import pro.datawiki.igaming.mdm.domain.MdmExternalSystem;
import pro.datawiki.igaming.mdm.domain.MdmSport;

@Entity
@Table(name="mdm_sport_ingestion_map", uniqueConstraints={@UniqueConstraint(name="uk_mdm_sport_map", columnNames={"external_system_id", "raw_key"})})
public class MdmSportIngestionMap {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="external_system_id", nullable=false)
    private MdmExternalSystem externalSystem;
    @Column(name="raw_key", nullable=false, length=128)
    private String rawKey;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="canonical_sport_id", nullable=false)
    private MdmSport canonicalSport;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Generated
    public static MdmSportIngestionMapBuilder builder() {
        return new MdmSportIngestionMapBuilder();
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
    public MdmSport getCanonicalSport() {
        return this.canonicalSport;
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
    public void setCanonicalSport(MdmSport canonicalSport) {
        this.canonicalSport = canonicalSport;
    }

    @Generated
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Generated
    public MdmSportIngestionMap() {
    }

    @Generated
    public MdmSportIngestionMap(Long id, MdmExternalSystem externalSystem, String rawKey, MdmSport canonicalSport, OffsetDateTime createdAt) {
        this.id = id;
        this.externalSystem = externalSystem;
        this.rawKey = rawKey;
        this.canonicalSport = canonicalSport;
        this.createdAt = createdAt;
    }

    @Generated
    public static class MdmSportIngestionMapBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmExternalSystem externalSystem;
        @Generated
        private String rawKey;
        @Generated
        private MdmSport canonicalSport;
        @Generated
        private OffsetDateTime createdAt;

        @Generated
        MdmSportIngestionMapBuilder() {
        }

        @Generated
        public MdmSportIngestionMapBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmSportIngestionMapBuilder externalSystem(MdmExternalSystem externalSystem) {
            this.externalSystem = externalSystem;
            return this;
        }

        @Generated
        public MdmSportIngestionMapBuilder rawKey(String rawKey) {
            this.rawKey = rawKey;
            return this;
        }

        @Generated
        public MdmSportIngestionMapBuilder canonicalSport(MdmSport canonicalSport) {
            this.canonicalSport = canonicalSport;
            return this;
        }

        @Generated
        public MdmSportIngestionMapBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmSportIngestionMap build() {
            return new MdmSportIngestionMap(this.id, this.externalSystem, this.rawKey, this.canonicalSport, this.createdAt);
        }

        @Generated
        public String toString() {
            return "MdmSportIngestionMap.MdmSportIngestionMapBuilder(id=" + this.id + ", externalSystem=" + String.valueOf(this.externalSystem) + ", rawKey=" + this.rawKey + ", canonicalSport=" + String.valueOf(this.canonicalSport) + ", createdAt=" + String.valueOf(this.createdAt) + ")";
        }
    }
}

