/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.Id
 *  jakarta.persistence.Table
 *  lombok.Generated
 *  org.hibernate.annotations.CreationTimestamp
 */
package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name="mdm_team_merge_map")
public class MdmTeamMergeMap {
    @Id
    @Column(name="local_id", nullable=false)
    private Long localId;
    @Column(name="sofa_id", nullable=false)
    private Long sofaId;
    @Column(name="local_name", nullable=false)
    private String localName;
    @Column(name="sofa_name", nullable=false)
    private String sofaName;
    @Column(name="sport_id", nullable=false)
    private Integer sportId;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Generated
    public static MdmTeamMergeMapBuilder builder() {
        return new MdmTeamMergeMapBuilder();
    }

    @Generated
    public Long getLocalId() {
        return this.localId;
    }

    @Generated
    public Long getSofaId() {
        return this.sofaId;
    }

    @Generated
    public String getLocalName() {
        return this.localName;
    }

    @Generated
    public String getSofaName() {
        return this.sofaName;
    }

    @Generated
    public Integer getSportId() {
        return this.sportId;
    }

    @Generated
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @Generated
    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    @Generated
    public void setSofaId(Long sofaId) {
        this.sofaId = sofaId;
    }

    @Generated
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Generated
    public void setSofaName(String sofaName) {
        this.sofaName = sofaName;
    }

    @Generated
    public void setSportId(Integer sportId) {
        this.sportId = sportId;
    }

    @Generated
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Generated
    public MdmTeamMergeMap() {
    }

    @Generated
    public MdmTeamMergeMap(Long localId, Long sofaId, String localName, String sofaName, Integer sportId, OffsetDateTime createdAt) {
        this.localId = localId;
        this.sofaId = sofaId;
        this.localName = localName;
        this.sofaName = sofaName;
        this.sportId = sportId;
        this.createdAt = createdAt;
    }

    @Generated
    public static class MdmTeamMergeMapBuilder {
        @Generated
        private Long localId;
        @Generated
        private Long sofaId;
        @Generated
        private String localName;
        @Generated
        private String sofaName;
        @Generated
        private Integer sportId;
        @Generated
        private OffsetDateTime createdAt;

        @Generated
        MdmTeamMergeMapBuilder() {
        }

        @Generated
        public MdmTeamMergeMapBuilder localId(Long localId) {
            this.localId = localId;
            return this;
        }

        @Generated
        public MdmTeamMergeMapBuilder sofaId(Long sofaId) {
            this.sofaId = sofaId;
            return this;
        }

        @Generated
        public MdmTeamMergeMapBuilder localName(String localName) {
            this.localName = localName;
            return this;
        }

        @Generated
        public MdmTeamMergeMapBuilder sofaName(String sofaName) {
            this.sofaName = sofaName;
            return this;
        }

        @Generated
        public MdmTeamMergeMapBuilder sportId(Integer sportId) {
            this.sportId = sportId;
            return this;
        }

        @Generated
        public MdmTeamMergeMapBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmTeamMergeMap build() {
            return new MdmTeamMergeMap(this.localId, this.sofaId, this.localName, this.sofaName, this.sportId, this.createdAt);
        }

        @Generated
        public String toString() {
            return "MdmTeamMergeMap.MdmTeamMergeMapBuilder(localId=" + this.localId + ", sofaId=" + this.sofaId + ", localName=" + this.localName + ", sofaName=" + this.sofaName + ", sportId=" + this.sportId + ", createdAt=" + String.valueOf(this.createdAt) + ")";
        }
    }
}

