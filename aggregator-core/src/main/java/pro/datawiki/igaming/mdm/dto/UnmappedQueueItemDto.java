/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pro.datawiki.igaming.mdm.dto;

import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.Generated;

public class UnmappedQueueItemDto
implements Serializable {
    private Long id;
    private String externalSystemCode;
    private String entityType;
    private String rawKey;
    private String sourceId;
    private String rawName;
    private Integer sportId;
    private String sportSlug;
    private Integer countryId;
    private String countryName;
    private String contextParentKey;
    private String contextParentName;
    private String status;
    private String resolutionNotes;
    private OffsetDateTime createdAt;

    @Generated
    public static UnmappedQueueItemDtoBuilder builder() {
        return new UnmappedQueueItemDtoBuilder();
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
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UnmappedQueueItemDto)) {
            return false;
        }
        UnmappedQueueItemDto other = (UnmappedQueueItemDto)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Integer this$sportId = this.getSportId();
        Integer other$sportId = other.getSportId();
        if (this$sportId == null ? other$sportId != null : !((Object)this$sportId).equals(other$sportId)) {
            return false;
        }
        Integer this$countryId = this.getCountryId();
        Integer other$countryId = other.getCountryId();
        if (this$countryId == null ? other$countryId != null : !((Object)this$countryId).equals(other$countryId)) {
            return false;
        }
        String this$externalSystemCode = this.getExternalSystemCode();
        String other$externalSystemCode = other.getExternalSystemCode();
        if (this$externalSystemCode == null ? other$externalSystemCode != null : !this$externalSystemCode.equals(other$externalSystemCode)) {
            return false;
        }
        String this$entityType = this.getEntityType();
        String other$entityType = other.getEntityType();
        if (this$entityType == null ? other$entityType != null : !this$entityType.equals(other$entityType)) {
            return false;
        }
        String this$rawKey = this.getRawKey();
        String other$rawKey = other.getRawKey();
        if (this$rawKey == null ? other$rawKey != null : !this$rawKey.equals(other$rawKey)) {
            return false;
        }
        String this$sourceId = this.getSourceId();
        String other$sourceId = other.getSourceId();
        if (this$sourceId == null ? other$sourceId != null : !this$sourceId.equals(other$sourceId)) {
            return false;
        }
        String this$rawName = this.getRawName();
        String other$rawName = other.getRawName();
        if (this$rawName == null ? other$rawName != null : !this$rawName.equals(other$rawName)) {
            return false;
        }
        String this$sportSlug = this.getSportSlug();
        String other$sportSlug = other.getSportSlug();
        if (this$sportSlug == null ? other$sportSlug != null : !this$sportSlug.equals(other$sportSlug)) {
            return false;
        }
        String this$countryName = this.getCountryName();
        String other$countryName = other.getCountryName();
        if (this$countryName == null ? other$countryName != null : !this$countryName.equals(other$countryName)) {
            return false;
        }
        String this$contextParentKey = this.getContextParentKey();
        String other$contextParentKey = other.getContextParentKey();
        if (this$contextParentKey == null ? other$contextParentKey != null : !this$contextParentKey.equals(other$contextParentKey)) {
            return false;
        }
        String this$contextParentName = this.getContextParentName();
        String other$contextParentName = other.getContextParentName();
        if (this$contextParentName == null ? other$contextParentName != null : !this$contextParentName.equals(other$contextParentName)) {
            return false;
        }
        String this$status = this.getStatus();
        String other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) {
            return false;
        }
        String this$resolutionNotes = this.getResolutionNotes();
        String other$resolutionNotes = other.getResolutionNotes();
        if (this$resolutionNotes == null ? other$resolutionNotes != null : !this$resolutionNotes.equals(other$resolutionNotes)) {
            return false;
        }
        OffsetDateTime this$createdAt = this.getCreatedAt();
        OffsetDateTime other$createdAt = other.getCreatedAt();
        return !(this$createdAt == null ? other$createdAt != null : !((Object)this$createdAt).equals(other$createdAt));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof UnmappedQueueItemDto;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Integer $sportId = this.getSportId();
        result = result * 59 + ($sportId == null ? 43 : ((Object)$sportId).hashCode());
        Integer $countryId = this.getCountryId();
        result = result * 59 + ($countryId == null ? 43 : ((Object)$countryId).hashCode());
        String $externalSystemCode = this.getExternalSystemCode();
        result = result * 59 + ($externalSystemCode == null ? 43 : $externalSystemCode.hashCode());
        String $entityType = this.getEntityType();
        result = result * 59 + ($entityType == null ? 43 : $entityType.hashCode());
        String $rawKey = this.getRawKey();
        result = result * 59 + ($rawKey == null ? 43 : $rawKey.hashCode());
        String $sourceId = this.getSourceId();
        result = result * 59 + ($sourceId == null ? 43 : $sourceId.hashCode());
        String $rawName = this.getRawName();
        result = result * 59 + ($rawName == null ? 43 : $rawName.hashCode());
        String $sportSlug = this.getSportSlug();
        result = result * 59 + ($sportSlug == null ? 43 : $sportSlug.hashCode());
        String $countryName = this.getCountryName();
        result = result * 59 + ($countryName == null ? 43 : $countryName.hashCode());
        String $contextParentKey = this.getContextParentKey();
        result = result * 59 + ($contextParentKey == null ? 43 : $contextParentKey.hashCode());
        String $contextParentName = this.getContextParentName();
        result = result * 59 + ($contextParentName == null ? 43 : $contextParentName.hashCode());
        String $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : $status.hashCode());
        String $resolutionNotes = this.getResolutionNotes();
        result = result * 59 + ($resolutionNotes == null ? 43 : $resolutionNotes.hashCode());
        OffsetDateTime $createdAt = this.getCreatedAt();
        result = result * 59 + ($createdAt == null ? 43 : ((Object)$createdAt).hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "UnmappedQueueItemDto(id=" + this.getId() + ", externalSystemCode=" + this.getExternalSystemCode() + ", entityType=" + this.getEntityType() + ", rawKey=" + this.getRawKey() + ", sourceId=" + this.getSourceId() + ", rawName=" + this.getRawName() + ", sportId=" + this.getSportId() + ", sportSlug=" + this.getSportSlug() + ", countryId=" + this.getCountryId() + ", countryName=" + this.getCountryName() + ", contextParentKey=" + this.getContextParentKey() + ", contextParentName=" + this.getContextParentName() + ", status=" + this.getStatus() + ", resolutionNotes=" + this.getResolutionNotes() + ", createdAt=" + String.valueOf(this.getCreatedAt()) + ")";
    }

    @Generated
    public UnmappedQueueItemDto() {
    }

    @Generated
    public UnmappedQueueItemDto(Long id, String externalSystemCode, String entityType, String rawKey, String sourceId, String rawName, Integer sportId, String sportSlug, Integer countryId, String countryName, String contextParentKey, String contextParentName, String status, String resolutionNotes, OffsetDateTime createdAt) {
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
    }

    @Generated
    public static class UnmappedQueueItemDtoBuilder {
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
        private String status;
        @Generated
        private String resolutionNotes;
        @Generated
        private OffsetDateTime createdAt;

        @Generated
        UnmappedQueueItemDtoBuilder() {
        }

        @Generated
        public UnmappedQueueItemDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder externalSystemCode(String externalSystemCode) {
            this.externalSystemCode = externalSystemCode;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder rawKey(String rawKey) {
            this.rawKey = rawKey;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder rawName(String rawName) {
            this.rawName = rawName;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder sportId(Integer sportId) {
            this.sportId = sportId;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder sportSlug(String sportSlug) {
            this.sportSlug = sportSlug;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder countryId(Integer countryId) {
            this.countryId = countryId;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder countryName(String countryName) {
            this.countryName = countryName;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder contextParentKey(String contextParentKey) {
            this.contextParentKey = contextParentKey;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder contextParentName(String contextParentName) {
            this.contextParentName = contextParentName;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder status(String status) {
            this.status = status;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder resolutionNotes(String resolutionNotes) {
            this.resolutionNotes = resolutionNotes;
            return this;
        }

        @Generated
        public UnmappedQueueItemDtoBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public UnmappedQueueItemDto build() {
            return new UnmappedQueueItemDto(this.id, this.externalSystemCode, this.entityType, this.rawKey, this.sourceId, this.rawName, this.sportId, this.sportSlug, this.countryId, this.countryName, this.contextParentKey, this.contextParentName, this.status, this.resolutionNotes, this.createdAt);
        }

        @Generated
        public String toString() {
            return "UnmappedQueueItemDto.UnmappedQueueItemDtoBuilder(id=" + this.id + ", externalSystemCode=" + this.externalSystemCode + ", entityType=" + this.entityType + ", rawKey=" + this.rawKey + ", sourceId=" + this.sourceId + ", rawName=" + this.rawName + ", sportId=" + this.sportId + ", sportSlug=" + this.sportSlug + ", countryId=" + this.countryId + ", countryName=" + this.countryName + ", contextParentKey=" + this.contextParentKey + ", contextParentName=" + this.contextParentName + ", status=" + this.status + ", resolutionNotes=" + this.resolutionNotes + ", createdAt=" + String.valueOf(this.createdAt) + ")";
        }
    }
}

