/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pro.datawiki.igaming.mdm.dto;

import java.io.Serializable;
import lombok.Generated;

public class ResolveTeamMappingRequest
implements Serializable {
    private Long queueItemId;
    private String externalSystemCode;
    private String rawKey;
    private String sourceId;
    private Long canonicalTeamId;
    private Long tournamentId;
    private String rawName;
    private String resolutionNotes;

    @Generated
    public static ResolveTeamMappingRequestBuilder builder() {
        return new ResolveTeamMappingRequestBuilder();
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
    public Long getCanonicalTeamId() {
        return this.canonicalTeamId;
    }

    @Generated
    public Long getTournamentId() {
        return this.tournamentId;
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
    public void setCanonicalTeamId(Long canonicalTeamId) {
        this.canonicalTeamId = canonicalTeamId;
    }

    @Generated
    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
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
        if (!(o instanceof ResolveTeamMappingRequest)) {
            return false;
        }
        ResolveTeamMappingRequest other = (ResolveTeamMappingRequest)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Long this$queueItemId = this.getQueueItemId();
        Long other$queueItemId = other.getQueueItemId();
        if (this$queueItemId == null ? other$queueItemId != null : !((Object)this$queueItemId).equals(other$queueItemId)) {
            return false;
        }
        Long this$canonicalTeamId = this.getCanonicalTeamId();
        Long other$canonicalTeamId = other.getCanonicalTeamId();
        if (this$canonicalTeamId == null ? other$canonicalTeamId != null : !((Object)this$canonicalTeamId).equals(other$canonicalTeamId)) {
            return false;
        }
        Long this$tournamentId = this.getTournamentId();
        Long other$tournamentId = other.getTournamentId();
        if (this$tournamentId == null ? other$tournamentId != null : !((Object)this$tournamentId).equals(other$tournamentId)) {
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
        return other instanceof ResolveTeamMappingRequest;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $queueItemId = this.getQueueItemId();
        result = result * 59 + ($queueItemId == null ? 43 : ((Object)$queueItemId).hashCode());
        Long $canonicalTeamId = this.getCanonicalTeamId();
        result = result * 59 + ($canonicalTeamId == null ? 43 : ((Object)$canonicalTeamId).hashCode());
        Long $tournamentId = this.getTournamentId();
        result = result * 59 + ($tournamentId == null ? 43 : ((Object)$tournamentId).hashCode());
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
        return "ResolveTeamMappingRequest(queueItemId=" + this.getQueueItemId() + ", externalSystemCode=" + this.getExternalSystemCode() + ", rawKey=" + this.getRawKey() + ", sourceId=" + this.getSourceId() + ", canonicalTeamId=" + this.getCanonicalTeamId() + ", tournamentId=" + this.getTournamentId() + ", rawName=" + this.getRawName() + ", resolutionNotes=" + this.getResolutionNotes() + ")";
    }

    @Generated
    public ResolveTeamMappingRequest() {
    }

    @Generated
    public ResolveTeamMappingRequest(Long queueItemId, String externalSystemCode, String rawKey, String sourceId, Long canonicalTeamId, Long tournamentId, String rawName, String resolutionNotes) {
        this.queueItemId = queueItemId;
        this.externalSystemCode = externalSystemCode;
        this.rawKey = rawKey;
        this.sourceId = sourceId;
        this.canonicalTeamId = canonicalTeamId;
        this.tournamentId = tournamentId;
        this.rawName = rawName;
        this.resolutionNotes = resolutionNotes;
    }

    @Generated
    public static class ResolveTeamMappingRequestBuilder {
        @Generated
        private Long queueItemId;
        @Generated
        private String externalSystemCode;
        @Generated
        private String rawKey;
        @Generated
        private String sourceId;
        @Generated
        private Long canonicalTeamId;
        @Generated
        private Long tournamentId;
        @Generated
        private String rawName;
        @Generated
        private String resolutionNotes;

        @Generated
        ResolveTeamMappingRequestBuilder() {
        }

        @Generated
        public ResolveTeamMappingRequestBuilder queueItemId(Long queueItemId) {
            this.queueItemId = queueItemId;
            return this;
        }

        @Generated
        public ResolveTeamMappingRequestBuilder externalSystemCode(String externalSystemCode) {
            this.externalSystemCode = externalSystemCode;
            return this;
        }

        @Generated
        public ResolveTeamMappingRequestBuilder rawKey(String rawKey) {
            this.rawKey = rawKey;
            return this;
        }

        @Generated
        public ResolveTeamMappingRequestBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        @Generated
        public ResolveTeamMappingRequestBuilder canonicalTeamId(Long canonicalTeamId) {
            this.canonicalTeamId = canonicalTeamId;
            return this;
        }

        @Generated
        public ResolveTeamMappingRequestBuilder tournamentId(Long tournamentId) {
            this.tournamentId = tournamentId;
            return this;
        }

        @Generated
        public ResolveTeamMappingRequestBuilder rawName(String rawName) {
            this.rawName = rawName;
            return this;
        }

        @Generated
        public ResolveTeamMappingRequestBuilder resolutionNotes(String resolutionNotes) {
            this.resolutionNotes = resolutionNotes;
            return this;
        }

        @Generated
        public ResolveTeamMappingRequest build() {
            return new ResolveTeamMappingRequest(this.queueItemId, this.externalSystemCode, this.rawKey, this.sourceId, this.canonicalTeamId, this.tournamentId, this.rawName, this.resolutionNotes);
        }

        @Generated
        public String toString() {
            return "ResolveTeamMappingRequest.ResolveTeamMappingRequestBuilder(queueItemId=" + this.queueItemId + ", externalSystemCode=" + this.externalSystemCode + ", rawKey=" + this.rawKey + ", sourceId=" + this.sourceId + ", canonicalTeamId=" + this.canonicalTeamId + ", tournamentId=" + this.tournamentId + ", rawName=" + this.rawName + ", resolutionNotes=" + this.resolutionNotes + ")";
        }
    }
}

