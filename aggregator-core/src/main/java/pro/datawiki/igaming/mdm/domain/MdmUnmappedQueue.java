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
@Table(name="mdm_unmapped_queue", uniqueConstraints={@UniqueConstraint(name="uk_mdm_unmapped_queue", columnNames={"external_system_code", "entity_type", "raw_key"})})
public class MdmUnmappedQueue {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="external_system_code", nullable=false, length=64)
    private String externalSystemCode;
    @Column(name="entity_type", nullable=false, length=32)
    private String entityType;
    @Column(name="raw_key", nullable=false, length=255)
    private String rawKey;
    @Column(name="source_id", length=128)
    private String sourceId;
    @Column(name="raw_name", length=255)
    private String rawName;
    @Column(name="sport_id")
    private Integer sportId;
    @Column(name="sport_slug", length=64)
    private String sportSlug;
    @Column(name="country_id")
    private Integer countryId;
    @Column(name="country_name", length=128)
    private String countryName;
    @Column(name="context_parent_key", length=255)
    private String contextParentKey;
    @Column(name="context_parent_name", length=255)
    private String contextParentName;
    @Column(name="status", nullable=false, length=32)
    private String status;
    @Column(name="resolution_notes", columnDefinition="TEXT")
    private String resolutionNotes;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static String $default$status() {
        return "PENDING";
    }

    @Generated
    public static MdmUnmappedQueueBuilder builder() {
        return new MdmUnmappedQueueBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public String getExternalSystemCode() {
        return this.externalSystemCode;
    }

    @Generated
    public String getEntityType() {
        return this.entityType;
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
    public String getRawName() {
        return this.rawName;
    }

    @Generated
    public Integer getSportId() {
        return this.sportId;
    }

    @Generated
    public String getSportSlug() {
        return this.sportSlug;
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
    public String getContextParentKey() {
        return this.contextParentKey;
    }

    @Generated
    public String getContextParentName() {
        return this.contextParentName;
    }

    @Generated
    public String getStatus() {
        return this.status;
    }

    @Generated
    public String getResolutionNotes() {
        return this.resolutionNotes;
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
    public void setExternalSystemCode(String externalSystemCode) {
        this.externalSystemCode = externalSystemCode;
    }

    @Generated
    public void setEntityType(String entityType) {
        this.entityType = entityType;
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
    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    @Generated
    public void setSportId(Integer sportId) {
        this.sportId = sportId;
    }

    @Generated
    public void setSportSlug(String sportSlug) {
        this.sportSlug = sportSlug;
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
    public void setContextParentKey(String contextParentKey) {
        this.contextParentKey = contextParentKey;
    }

    @Generated
    public void setContextParentName(String contextParentName) {
        this.contextParentName = contextParentName;
    }

    @Generated
    public void setStatus(String status) {
        this.status = status;
    }

    @Generated
    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
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
    public MdmUnmappedQueue() {
        this.status = MdmUnmappedQueue.$default$status();
    }

    @Generated
    public MdmUnmappedQueue(Long id, String externalSystemCode, String entityType, String rawKey, String sourceId, String rawName, Integer sportId, String sportSlug, Integer countryId, String countryName, String contextParentKey, String contextParentName, String status, String resolutionNotes, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.externalSystemCode = externalSystemCode;
        this.entityType = entityType;
        this.rawKey = rawKey;
        this.sourceId = sourceId;
        this.rawName = rawName;
        this.sportId = sportId;
        this.sportSlug = sportSlug;
        this.countryId = countryId;
        this.countryName = countryName;
        this.contextParentKey = contextParentKey;
        this.contextParentName = contextParentName;
        this.status = status;
        this.resolutionNotes = resolutionNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmUnmappedQueueBuilder {
        @Generated
        private Long id;
        @Generated
        private String externalSystemCode;
        @Generated
        private String entityType;
        @Generated
        private String rawKey;
        @Generated
        private String sourceId;
        @Generated
        private String rawName;
        @Generated
        private Integer sportId;
        @Generated
        private String sportSlug;
        @Generated
        private Integer countryId;
        @Generated
        private String countryName;
        @Generated
        private String contextParentKey;
        @Generated
        private String contextParentName;
        @Generated
        private boolean status$set;
        @Generated
        private String status$value;
        @Generated
        private String resolutionNotes;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmUnmappedQueueBuilder() {
        }

        @Generated
        public MdmUnmappedQueueBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder externalSystemCode(String externalSystemCode) {
            this.externalSystemCode = externalSystemCode;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder rawKey(String rawKey) {
            this.rawKey = rawKey;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder rawName(String rawName) {
            this.rawName = rawName;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder sportId(Integer sportId) {
            this.sportId = sportId;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder sportSlug(String sportSlug) {
            this.sportSlug = sportSlug;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder countryId(Integer countryId) {
            this.countryId = countryId;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder countryName(String countryName) {
            this.countryName = countryName;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder contextParentKey(String contextParentKey) {
            this.contextParentKey = contextParentKey;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder contextParentName(String contextParentName) {
            this.contextParentName = contextParentName;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder status(String status) {
            this.status$value = status;
            this.status$set = true;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder resolutionNotes(String resolutionNotes) {
            this.resolutionNotes = resolutionNotes;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmUnmappedQueueBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmUnmappedQueue build() {
            String status$value = this.status$value;
            if (!this.status$set) {
                status$value = MdmUnmappedQueue.$default$status();
            }
            return new MdmUnmappedQueue(this.id, this.externalSystemCode, this.entityType, this.rawKey, this.sourceId, this.rawName, this.sportId, this.sportSlug, this.countryId, this.countryName, this.contextParentKey, this.contextParentName, status$value, this.resolutionNotes, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmUnmappedQueue.MdmUnmappedQueueBuilder(id=" + this.id + ", externalSystemCode=" + this.externalSystemCode + ", entityType=" + this.entityType + ", rawKey=" + this.rawKey + ", sourceId=" + this.sourceId + ", rawName=" + this.rawName + ", sportId=" + this.sportId + ", sportSlug=" + this.sportSlug + ", countryId=" + this.countryId + ", countryName=" + this.countryName + ", contextParentKey=" + this.contextParentKey + ", contextParentName=" + this.contextParentName + ", status$value=" + this.status$value + ", resolutionNotes=" + this.resolutionNotes + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

