/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.MapsId
 *  jakarta.persistence.OneToOne
 *  jakarta.persistence.Table
 *  lombok.Generated
 *  org.hibernate.annotations.JdbcTypeCode
 *  org.hibernate.annotations.UpdateTimestamp
 */
package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import pro.datawiki.igaming.mdm.domain.MdmTeam;

@Entity
@Table(name="mdm_team_dna")
public class MdmTeamDna {
    @Id
    @Column(name="team_id")
    private Long teamId;
    @OneToOne(fetch=FetchType.LAZY)
    @MapsId
    @JoinColumn(name="team_id")
    private MdmTeam team;
    @Column(name="primary_formation", length=32)
    private String primaryFormation;
    @Column(name="play_style", length=64)
    private String playStyle;
    @Column(name="pressing_intensity", precision=5, scale=2)
    private BigDecimal pressingIntensity;
    @Column(name="possession_avg_pct", precision=5, scale=2)
    private BigDecimal possessionAvgPct;
    @Column(name="aerial_threat_score", precision=5, scale=2)
    private BigDecimal aerialThreatScore;
    @Column(name="defense_vulnerability_score", precision=5, scale=2)
    private BigDecimal defenseVulnerabilityScore;
    @Column(name="reference_tier")
    private Integer referenceTier;
    @JdbcTypeCode(value=3001)
    @Column(name="similar_teams", columnDefinition="jsonb")
    private Object similarTeams;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    public static MdmTeamDnaBuilder builder() {
        return new MdmTeamDnaBuilder();
    }

    @Generated
    public Long getTeamId() {
        return this.teamId;
    }

    @Generated
    public MdmTeam getTeam() {
        return this.team;
    }

    @Generated
    public String getPrimaryFormation() {
        return this.primaryFormation;
    }

    @Generated
    public String getPlayStyle() {
        return this.playStyle;
    }

    @Generated
    public BigDecimal getPressingIntensity() {
        return this.pressingIntensity;
    }

    @Generated
    public BigDecimal getPossessionAvgPct() {
        return this.possessionAvgPct;
    }

    @Generated
    public BigDecimal getAerialThreatScore() {
        return this.aerialThreatScore;
    }

    @Generated
    public BigDecimal getDefenseVulnerabilityScore() {
        return this.defenseVulnerabilityScore;
    }

    @Generated
    public Integer getReferenceTier() {
        return this.referenceTier;
    }

    @Generated
    public Object getSimilarTeams() {
        return this.similarTeams;
    }

    @Generated
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @Generated
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    @Generated
    public void setTeam(MdmTeam team) {
        this.team = team;
    }

    @Generated
    public void setPrimaryFormation(String primaryFormation) {
        this.primaryFormation = primaryFormation;
    }

    @Generated
    public void setPlayStyle(String playStyle) {
        this.playStyle = playStyle;
    }

    @Generated
    public void setPressingIntensity(BigDecimal pressingIntensity) {
        this.pressingIntensity = pressingIntensity;
    }

    @Generated
    public void setPossessionAvgPct(BigDecimal possessionAvgPct) {
        this.possessionAvgPct = possessionAvgPct;
    }

    @Generated
    public void setAerialThreatScore(BigDecimal aerialThreatScore) {
        this.aerialThreatScore = aerialThreatScore;
    }

    @Generated
    public void setDefenseVulnerabilityScore(BigDecimal defenseVulnerabilityScore) {
        this.defenseVulnerabilityScore = defenseVulnerabilityScore;
    }

    @Generated
    public void setReferenceTier(Integer referenceTier) {
        this.referenceTier = referenceTier;
    }

    @Generated
    public void setSimilarTeams(Object similarTeams) {
        this.similarTeams = similarTeams;
    }

    @Generated
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Generated
    public MdmTeamDna() {
    }

    @Generated
    public MdmTeamDna(Long teamId, MdmTeam team, String primaryFormation, String playStyle, BigDecimal pressingIntensity, BigDecimal possessionAvgPct, BigDecimal aerialThreatScore, BigDecimal defenseVulnerabilityScore, Integer referenceTier, Object similarTeams, OffsetDateTime updatedAt) {
        this.teamId = teamId;
        this.team = team;
        this.primaryFormation = primaryFormation;
        this.playStyle = playStyle;
        this.pressingIntensity = pressingIntensity;
        this.possessionAvgPct = possessionAvgPct;
        this.aerialThreatScore = aerialThreatScore;
        this.defenseVulnerabilityScore = defenseVulnerabilityScore;
        this.referenceTier = referenceTier;
        this.similarTeams = similarTeams;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmTeamDnaBuilder {
        @Generated
        private Long teamId;
        @Generated
        private MdmTeam team;
        @Generated
        private String primaryFormation;
        @Generated
        private String playStyle;
        @Generated
        private BigDecimal pressingIntensity;
        @Generated
        private BigDecimal possessionAvgPct;
        @Generated
        private BigDecimal aerialThreatScore;
        @Generated
        private BigDecimal defenseVulnerabilityScore;
        @Generated
        private Integer referenceTier;
        @Generated
        private Object similarTeams;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmTeamDnaBuilder() {
        }

        @Generated
        public MdmTeamDnaBuilder teamId(Long teamId) {
            this.teamId = teamId;
            return this;
        }

        @Generated
        public MdmTeamDnaBuilder team(MdmTeam team) {
            this.team = team;
            return this;
        }

        @Generated
        public MdmTeamDnaBuilder primaryFormation(String primaryFormation) {
            this.primaryFormation = primaryFormation;
            return this;
        }

        @Generated
        public MdmTeamDnaBuilder playStyle(String playStyle) {
            this.playStyle = playStyle;
            return this;
        }

        @Generated
        public MdmTeamDnaBuilder pressingIntensity(BigDecimal pressingIntensity) {
            this.pressingIntensity = pressingIntensity;
            return this;
        }

        @Generated
        public MdmTeamDnaBuilder possessionAvgPct(BigDecimal possessionAvgPct) {
            this.possessionAvgPct = possessionAvgPct;
            return this;
        }

        @Generated
        public MdmTeamDnaBuilder aerialThreatScore(BigDecimal aerialThreatScore) {
            this.aerialThreatScore = aerialThreatScore;
            return this;
        }

        @Generated
        public MdmTeamDnaBuilder defenseVulnerabilityScore(BigDecimal defenseVulnerabilityScore) {
            this.defenseVulnerabilityScore = defenseVulnerabilityScore;
            return this;
        }

        @Generated
        public MdmTeamDnaBuilder referenceTier(Integer referenceTier) {
            this.referenceTier = referenceTier;
            return this;
        }

        @Generated
        public MdmTeamDnaBuilder similarTeams(Object similarTeams) {
            this.similarTeams = similarTeams;
            return this;
        }

        @Generated
        public MdmTeamDnaBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmTeamDna build() {
            return new MdmTeamDna(this.teamId, this.team, this.primaryFormation, this.playStyle, this.pressingIntensity, this.possessionAvgPct, this.aerialThreatScore, this.defenseVulnerabilityScore, this.referenceTier, this.similarTeams, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmTeamDna.MdmTeamDnaBuilder(teamId=" + this.teamId + ", team=" + String.valueOf(this.team) + ", primaryFormation=" + this.primaryFormation + ", playStyle=" + this.playStyle + ", pressingIntensity=" + String.valueOf(this.pressingIntensity) + ", possessionAvgPct=" + String.valueOf(this.possessionAvgPct) + ", aerialThreatScore=" + String.valueOf(this.aerialThreatScore) + ", defenseVulnerabilityScore=" + String.valueOf(this.defenseVulnerabilityScore) + ", referenceTier=" + this.referenceTier + ", similarTeams=" + String.valueOf(this.similarTeams) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

