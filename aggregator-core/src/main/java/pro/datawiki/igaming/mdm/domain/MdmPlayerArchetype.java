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
import pro.datawiki.igaming.mdm.domain.MdmPlayer;

@Entity
@Table(name="mdm_player_archetype")
public class MdmPlayerArchetype {
    @Id
    @Column(name="player_id")
    private Long playerId;
    @OneToOne(fetch=FetchType.LAZY)
    @MapsId
    @JoinColumn(name="player_id")
    private MdmPlayer player;
    @Column(length=64)
    private String archetype;
    @Column(name="overall_rating", precision=4, scale=2)
    private BigDecimal overallRating;
    @Column(name="is_joker_candidate", nullable=false)
    private Boolean isJokerCandidate;
    @Column(name="preferred_foot", length=16)
    private String preferredFoot;
    @Column(name="pace_rating", precision=4, scale=2)
    private BigDecimal paceRating;
    @Column(name="shooting_rating", precision=4, scale=2)
    private BigDecimal shootingRating;
    @Column(name="passing_rating", precision=4, scale=2)
    private BigDecimal passingRating;
    @Column(name="reference_tier")
    private Integer referenceTier;
    @JdbcTypeCode(value=3001)
    @Column(name="similar_players", columnDefinition="jsonb")
    private Object similarPlayers;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static Boolean $default$isJokerCandidate() {
        return false;
    }

    @Generated
    public static MdmPlayerArchetypeBuilder builder() {
        return new MdmPlayerArchetypeBuilder();
    }

    @Generated
    public Long getPlayerId() {
        return this.playerId;
    }

    @Generated
    public MdmPlayer getPlayer() {
        return this.player;
    }

    @Generated
    public String getArchetype() {
        return this.archetype;
    }

    @Generated
    public BigDecimal getOverallRating() {
        return this.overallRating;
    }

    @Generated
    public Boolean getIsJokerCandidate() {
        return this.isJokerCandidate;
    }

    @Generated
    public String getPreferredFoot() {
        return this.preferredFoot;
    }

    @Generated
    public BigDecimal getPaceRating() {
        return this.paceRating;
    }

    @Generated
    public BigDecimal getShootingRating() {
        return this.shootingRating;
    }

    @Generated
    public BigDecimal getPassingRating() {
        return this.passingRating;
    }

    @Generated
    public Integer getReferenceTier() {
        return this.referenceTier;
    }

    @Generated
    public Object getSimilarPlayers() {
        return this.similarPlayers;
    }

    @Generated
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    @Generated
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    @Generated
    public void setPlayer(MdmPlayer player) {
        this.player = player;
    }

    @Generated
    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    @Generated
    public void setOverallRating(BigDecimal overallRating) {
        this.overallRating = overallRating;
    }

    @Generated
    public void setIsJokerCandidate(Boolean isJokerCandidate) {
        this.isJokerCandidate = isJokerCandidate;
    }

    @Generated
    public void setPreferredFoot(String preferredFoot) {
        this.preferredFoot = preferredFoot;
    }

    @Generated
    public void setPaceRating(BigDecimal paceRating) {
        this.paceRating = paceRating;
    }

    @Generated
    public void setShootingRating(BigDecimal shootingRating) {
        this.shootingRating = shootingRating;
    }

    @Generated
    public void setPassingRating(BigDecimal passingRating) {
        this.passingRating = passingRating;
    }

    @Generated
    public void setReferenceTier(Integer referenceTier) {
        this.referenceTier = referenceTier;
    }

    @Generated
    public void setSimilarPlayers(Object similarPlayers) {
        this.similarPlayers = similarPlayers;
    }

    @Generated
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Generated
    public MdmPlayerArchetype() {
        this.isJokerCandidate = MdmPlayerArchetype.$default$isJokerCandidate();
    }

    @Generated
    public MdmPlayerArchetype(Long playerId, MdmPlayer player, String archetype, BigDecimal overallRating, Boolean isJokerCandidate, String preferredFoot, BigDecimal paceRating, BigDecimal shootingRating, BigDecimal passingRating, Integer referenceTier, Object similarPlayers, OffsetDateTime updatedAt) {
        this.playerId = playerId;
        this.player = player;
        this.archetype = archetype;
        this.overallRating = overallRating;
        this.isJokerCandidate = isJokerCandidate;
        this.preferredFoot = preferredFoot;
        this.paceRating = paceRating;
        this.shootingRating = shootingRating;
        this.passingRating = passingRating;
        this.referenceTier = referenceTier;
        this.similarPlayers = similarPlayers;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmPlayerArchetypeBuilder {
        @Generated
        private Long playerId;
        @Generated
        private MdmPlayer player;
        @Generated
        private String archetype;
        @Generated
        private BigDecimal overallRating;
        @Generated
        private boolean isJokerCandidate$set;
        @Generated
        private Boolean isJokerCandidate$value;
        @Generated
        private String preferredFoot;
        @Generated
        private BigDecimal paceRating;
        @Generated
        private BigDecimal shootingRating;
        @Generated
        private BigDecimal passingRating;
        @Generated
        private Integer referenceTier;
        @Generated
        private Object similarPlayers;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmPlayerArchetypeBuilder() {
        }

        @Generated
        public MdmPlayerArchetypeBuilder playerId(Long playerId) {
            this.playerId = playerId;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder player(MdmPlayer player) {
            this.player = player;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder archetype(String archetype) {
            this.archetype = archetype;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder overallRating(BigDecimal overallRating) {
            this.overallRating = overallRating;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder isJokerCandidate(Boolean isJokerCandidate) {
            this.isJokerCandidate$value = isJokerCandidate;
            this.isJokerCandidate$set = true;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder preferredFoot(String preferredFoot) {
            this.preferredFoot = preferredFoot;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder paceRating(BigDecimal paceRating) {
            this.paceRating = paceRating;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder shootingRating(BigDecimal shootingRating) {
            this.shootingRating = shootingRating;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder passingRating(BigDecimal passingRating) {
            this.passingRating = passingRating;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder referenceTier(Integer referenceTier) {
            this.referenceTier = referenceTier;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder similarPlayers(Object similarPlayers) {
            this.similarPlayers = similarPlayers;
            return this;
        }

        @Generated
        public MdmPlayerArchetypeBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmPlayerArchetype build() {
            Boolean isJokerCandidate$value = this.isJokerCandidate$value;
            if (!this.isJokerCandidate$set) {
                isJokerCandidate$value = MdmPlayerArchetype.$default$isJokerCandidate();
            }
            return new MdmPlayerArchetype(this.playerId, this.player, this.archetype, this.overallRating, isJokerCandidate$value, this.preferredFoot, this.paceRating, this.shootingRating, this.passingRating, this.referenceTier, this.similarPlayers, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmPlayerArchetype.MdmPlayerArchetypeBuilder(playerId=" + this.playerId + ", player=" + String.valueOf(this.player) + ", archetype=" + this.archetype + ", overallRating=" + String.valueOf(this.overallRating) + ", isJokerCandidate$value=" + this.isJokerCandidate$value + ", preferredFoot=" + this.preferredFoot + ", paceRating=" + String.valueOf(this.paceRating) + ", shootingRating=" + String.valueOf(this.shootingRating) + ", passingRating=" + String.valueOf(this.passingRating) + ", referenceTier=" + this.referenceTier + ", similarPlayers=" + String.valueOf(this.similarPlayers) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

