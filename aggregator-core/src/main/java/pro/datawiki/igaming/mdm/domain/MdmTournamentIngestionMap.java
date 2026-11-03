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
import pro.datawiki.igaming.mdm.domain.MdmTournament;

@Entity
@Table(name="mdm_tournament_ingestion_map", uniqueConstraints={@UniqueConstraint(name="uk_mdm_tournament_map", columnNames={"external_system_id", "raw_key"})})
public class MdmTournamentIngestionMap {
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
    @JoinColumn(name="canonical_tournament_id", nullable=false)
    private MdmTournament canonicalTournament;
    @Column(name="raw_name")
    private String rawName;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Generated
    public static MdmTournamentIngestionMapBuilder builder() {
        return new MdmTournamentIngestionMapBuilder();
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
    public MdmTournament getCanonicalTournament() {
        return this.canonicalTournament;
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
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    @Generated
    public void setCanonicalTournament(MdmTournament canonicalTournament) {
        this.canonicalTournament = canonicalTournament;
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
    public MdmTournamentIngestionMap() {
    }

    @Generated
    public MdmTournamentIngestionMap(Long id, MdmExternalSystem externalSystem, String rawKey, String sourceId, MdmTournament canonicalTournament, String rawName, OffsetDateTime createdAt) {
        this.id = id;
        this.externalSystem = externalSystem;
        this.rawKey = rawKey;
        this.sourceId = sourceId;
        this.canonicalTournament = canonicalTournament;
        this.rawName = rawName;
        this.createdAt = createdAt;
    }

    @Generated
    public static class MdmTournamentIngestionMapBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmExternalSystem externalSystem;
        @Generated
        private String rawKey;
        @Generated
        private String sourceId;
        @Generated
        private MdmTournament canonicalTournament;
        @Generated
        private String rawName;
        @Generated
        private OffsetDateTime createdAt;

        @Generated
        MdmTournamentIngestionMapBuilder() {
        }

        @Generated
        public MdmTournamentIngestionMapBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmTournamentIngestionMapBuilder externalSystem(MdmExternalSystem externalSystem) {
            this.externalSystem = externalSystem;
            return this;
        }

        @Generated
        public MdmTournamentIngestionMapBuilder rawKey(String rawKey) {
            this.rawKey = rawKey;
            return this;
        }

        @Generated
        public MdmTournamentIngestionMapBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        @Generated
        public MdmTournamentIngestionMapBuilder canonicalTournament(MdmTournament canonicalTournament) {
            this.canonicalTournament = canonicalTournament;
            return this;
        }

        @Generated
        public MdmTournamentIngestionMapBuilder rawName(String rawName) {
            this.rawName = rawName;
            return this;
        }

        @Generated
        public MdmTournamentIngestionMapBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmTournamentIngestionMap build() {
            return new MdmTournamentIngestionMap(this.id, this.externalSystem, this.rawKey, this.sourceId, this.canonicalTournament, this.rawName, this.createdAt);
        }

        @Generated
        public String toString() {
            return "MdmTournamentIngestionMap.MdmTournamentIngestionMapBuilder(id=" + this.id + ", externalSystem=" + String.valueOf(this.externalSystem) + ", rawKey=" + this.rawKey + ", sourceId=" + this.sourceId + ", canonicalTournament=" + String.valueOf(this.canonicalTournament) + ", rawName=" + this.rawName + ", createdAt=" + String.valueOf(this.createdAt) + ")";
        }
    }
}

