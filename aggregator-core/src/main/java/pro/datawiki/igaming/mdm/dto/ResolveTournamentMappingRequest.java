/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pro.datawiki.igaming.mdm.dto;

import java.io.Serializable;
import lombok.Generated;

public class ResolveTournamentMappingRequest
implements Serializable {
    private Long queueItemId;
    private String externalSystemCode;
    private String rawKey;
    private String sourceId;
    private Long canonicalTournamentId;
    private String rawName;
    private String resolutionNotes;

    @Generated
    public static ResolveTournamentMappingRequestBuilder builder() {
        return new ResolveTournamentMappingRequestBuilder();
    }

    @Generated
    public Long getQueueItemId() {
        return this.queueItemId;
    }

    @Generated
    public String getExternalSystemCode() {
        return this.externalSystemCode;
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
    public Long getCanonicalTournamentId() {
        return this.canonicalTournamentId;
    }

    @Generated
    public String getRawName() {
        return this.rawName;
    }

    @Generated
    public String getResolutionNotes() {
        return this.resolutionNotes;
    }

    @Generated
    public void setQueueItemId(Long queueItemId) {
        this.queueItemId = queueItemId;
    }

    @Generated
    public void setExternalSystemCode(String externalSystemCode) {
        this.externalSystemCode = externalSystemCode;
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
    public void setCanonicalTournamentId(Long canonicalTournamentId) {
        this.canonicalTournamentId = canonicalTournamentId;
    }

    @Generated
    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    @Generated
    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ResolveTournamentMappingRequest)) {
            return false;
        }
        ResolveTournamentMappingRequest other = (ResolveTournamentMappingRequest)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$queueItemId = this.getQueueItemId();
        Long other$queueItemId = other.getQueueItemId();
        if (this$queueItemId == null ? other$queueItemId != null : !((Object)this$queueItemId).equals(other$queueItemId)) {
            return false;
        }
        Long this$canonicalTournamentId = this.getCanonicalTournamentId();
        Long other$canonicalTournamentId = other.getCanonicalTournamentId();
        if (this$canonicalTournamentId == null ? other$canonicalTournamentId != null : !((Object)this$canonicalTournamentId).equals(other$canonicalTournamentId)) {
            return false;
        }
        String this$externalSystemCode = this.getExternalSystemCode();
        String other$externalSystemCode = other.getExternalSystemCode();
        if (this$externalSystemCode == null ? other$externalSystemCode != null : !this$externalSystemCode.equals(other$externalSystemCode)) {
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
        String this$resolutionNotes = this.getResolutionNotes();
        String other$resolutionNotes = other.getResolutionNotes();
        return !(this$resolutionNotes == null ? other$resolutionNotes != null : !this$resolutionNotes.equals(other$resolutionNotes));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof ResolveTournamentMappingRequest;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $queueItemId = this.getQueueItemId();
        result = result * 59 + ($queueItemId == null ? 43 : ((Object)$queueItemId).hashCode());
        Long $canonicalTournamentId = this.getCanonicalTournamentId();
        result = result * 59 + ($canonicalTournamentId == null ? 43 : ((Object)$canonicalTournamentId).hashCode());
        String $externalSystemCode = this.getExternalSystemCode();
        result = result * 59 + ($externalSystemCode == null ? 43 : $externalSystemCode.hashCode());
        String $rawKey = this.getRawKey();
        result = result * 59 + ($rawKey == null ? 43 : $rawKey.hashCode());
        String $sourceId = this.getSourceId();
        result = result * 59 + ($sourceId == null ? 43 : $sourceId.hashCode());
        String $rawName = this.getRawName();
        result = result * 59 + ($rawName == null ? 43 : $rawName.hashCode());
        String $resolutionNotes = this.getResolutionNotes();
        result = result * 59 + ($resolutionNotes == null ? 43 : $resolutionNotes.hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "ResolveTournamentMappingRequest(queueItemId=" + this.getQueueItemId() + ", externalSystemCode=" + this.getExternalSystemCode() + ", rawKey=" + this.getRawKey() + ", sourceId=" + this.getSourceId() + ", canonicalTournamentId=" + this.getCanonicalTournamentId() + ", rawName=" + this.getRawName() + ", resolutionNotes=" + this.getResolutionNotes() + ")";
    }

    @Generated
    public ResolveTournamentMappingRequest() {
    }

    @Generated
    public ResolveTournamentMappingRequest(Long queueItemId, String externalSystemCode, String rawKey, String sourceId, Long canonicalTournamentId, String rawName, String resolutionNotes) {
        this.queueItemId = queueItemId;
        this.externalSystemCode = externalSystemCode;
        this.rawKey = rawKey;
        this.sourceId = sourceId;
        this.canonicalTournamentId = canonicalTournamentId;
        this.rawName = rawName;
        this.resolutionNotes = resolutionNotes;
    }

    @Generated
    public static class ResolveTournamentMappingRequestBuilder {
        @Generated
        private Long queueItemId;
        @Generated
        private String externalSystemCode;
        @Generated
        private String rawKey;
        @Generated
        private String sourceId;
        @Generated
        private Long canonicalTournamentId;
        @Generated
        private String rawName;
        @Generated
        private String resolutionNotes;

        @Generated
        ResolveTournamentMappingRequestBuilder() {
        }

        @Generated
        public ResolveTournamentMappingRequestBuilder queueItemId(Long queueItemId) {
            this.queueItemId = queueItemId;
            return this;
        }

        @Generated
        public ResolveTournamentMappingRequestBuilder externalSystemCode(String externalSystemCode) {
            this.externalSystemCode = externalSystemCode;
            return this;
        }

        @Generated
        public ResolveTournamentMappingRequestBuilder rawKey(String rawKey) {
            this.rawKey = rawKey;
            return this;
        }

        @Generated
        public ResolveTournamentMappingRequestBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        @Generated
        public ResolveTournamentMappingRequestBuilder canonicalTournamentId(Long canonicalTournamentId) {
            this.canonicalTournamentId = canonicalTournamentId;
            return this;
        }

        @Generated
        public ResolveTournamentMappingRequestBuilder rawName(String rawName) {
            this.rawName = rawName;
            return this;
        }

        @Generated
        public ResolveTournamentMappingRequestBuilder resolutionNotes(String resolutionNotes) {
            this.resolutionNotes = resolutionNotes;
            return this;
        }

        @Generated
        public ResolveTournamentMappingRequest build() {
            return new ResolveTournamentMappingRequest(this.queueItemId, this.externalSystemCode, this.rawKey, this.sourceId, this.canonicalTournamentId, this.rawName, this.resolutionNotes);
        }

        @Generated
        public String toString() {
            return "ResolveTournamentMappingRequest.ResolveTournamentMappingRequestBuilder(queueItemId=" + this.queueItemId + ", externalSystemCode=" + this.externalSystemCode + ", rawKey=" + this.rawKey + ", sourceId=" + this.sourceId + ", canonicalTournamentId=" + this.canonicalTournamentId + ", rawName=" + this.rawName + ", resolutionNotes=" + this.resolutionNotes + ")";
        }
    }
}

