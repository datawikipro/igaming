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
import pro.datawiki.igaming.mdm.domain.MdmTeam;

@Entity
@Table(name="mdm_team_ingestion_map", uniqueConstraints={@UniqueConstraint(name="uk_mdm_team_map", columnNames={"external_system_id", "raw_key"})})
public class MdmTeamIngestionMap {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="external_system_id", nullable=false)
    private MdmExternalSystem externalSystem;
    @Column(name="raw_key", nullable=false)
    private String rawKey;
    @Column(name="source_id")
    private String sourceId;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="canonical_team_id", nullable=false)
    private MdmTeam canonicalTeam;
    @Column(name="raw_name")
    private String rawName;
    @Column(name="is_verified", nullable=false)
    private Boolean isVerified;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Generated
    private static Boolean $default$isVerified() {
        return true;
    }

    @Generated
    public static MdmTeamIngestionMapBuilder builder() {
        return new MdmTeamIngestionMapBuilder();
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
    public String getSourceId() {
        return this.sourceId;
    }

    @Generated
    public MdmTeam getCanonicalTeam() {
        return this.canonicalTeam;
    }

    @Generated
    public String getRawName() {
        return this.rawName;
    }

    @Generated
    public Boolean getIsVerified() {
        return this.isVerified;
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
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    @Generated
    public void setCanonicalTeam(MdmTeam canonicalTeam) {
        this.canonicalTeam = canonicalTeam;
    }

    @Generated
    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    @Generated
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    @Generated
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Generated
    public MdmTeamIngestionMap() {
        this.isVerified = MdmTeamIngestionMap.$default$isVerified();
    }

    @Generated
    public MdmTeamIngestionMap(Long id, MdmExternalSystem externalSystem, String rawKey, String sourceId, MdmTeam canonicalTeam, String rawName, Boolean isVerified, OffsetDateTime createdAt) {
        this.id = id;
        this.externalSystem = externalSystem;
        this.rawKey = rawKey;
        this.sourceId = sourceId;
        this.canonicalTeam = canonicalTeam;
        this.rawName = rawName;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
    }

    @Generated
    public static class MdmTeamIngestionMapBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmExternalSystem externalSystem;
        @Generated
        private String rawKey;
        @Generated
        private String sourceId;
        @Generated
        private MdmTeam canonicalTeam;
        @Generated
        private String rawName;
        @Generated
        private boolean isVerified$set;
        @Generated
        private Boolean isVerified$value;
        @Generated
        private OffsetDateTime createdAt;

        @Generated
        MdmTeamIngestionMapBuilder() {
        }

        @Generated
        public MdmTeamIngestionMapBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmTeamIngestionMapBuilder externalSystem(MdmExternalSystem externalSystem) {
            this.externalSystem = externalSystem;
            return this;
        }

        @Generated
        public MdmTeamIngestionMapBuilder rawKey(String rawKey) {
            this.rawKey = rawKey;
            return this;
        }

        @Generated
        public MdmTeamIngestionMapBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        @Generated
        public MdmTeamIngestionMapBuilder canonicalTeam(MdmTeam canonicalTeam) {
            this.canonicalTeam = canonicalTeam;
            return this;
        }

        @Generated
        public MdmTeamIngestionMapBuilder rawName(String rawName) {
            this.rawName = rawName;
            return this;
        }

        @Generated
        public MdmTeamIngestionMapBuilder isVerified(Boolean isVerified) {
            this.isVerified$value = isVerified;
            this.isVerified$set = true;
            return this;
        }

        @Generated
        public MdmTeamIngestionMapBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmTeamIngestionMap build() {
            Boolean isVerified$value = this.isVerified$value;
            if (!this.isVerified$set) {
                isVerified$value = MdmTeamIngestionMap.$default$isVerified();
            }
            return new MdmTeamIngestionMap(this.id, this.externalSystem, this.rawKey, this.sourceId, this.canonicalTeam, this.rawName, isVerified$value, this.createdAt);
        }

        @Generated
        public String toString() {
            return "MdmTeamIngestionMap.MdmTeamIngestionMapBuilder(id=" + this.id + ", externalSystem=" + String.valueOf(this.externalSystem) + ", rawKey=" + this.rawKey + ", sourceId=" + this.sourceId + ", canonicalTeam=" + String.valueOf(this.canonicalTeam) + ", rawName=" + this.rawName + ", isVerified$value=" + this.isVerified$value + ", createdAt=" + String.valueOf(this.createdAt) + ")";
        }
    }
}

