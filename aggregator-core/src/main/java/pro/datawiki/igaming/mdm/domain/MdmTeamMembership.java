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
 *  org.hibernate.annotations.UpdateTimestamp
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pro.datawiki.igaming.mdm.domain.MdmPlayer;
import pro.datawiki.igaming.mdm.domain.MdmTeam;

@Entity
@Table(name="mdm_team_membership", uniqueConstraints={@UniqueConstraint(name="uk_mdm_player_team_membership", columnNames={"player_id", "team_id", "membership_type"})})
public class MdmTeamMembership {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="player_id", nullable=false)
    private MdmPlayer player;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="team_id", nullable=false)
    private MdmTeam team;
    @Column(name="membership_type", nullable=false, length=32)
    private String membershipType;
    @Column(name="jersey_number")
    private Integer jerseyNumber;
    @Column(name="squad_role", nullable=false, length=32)
    private String squadRole;
    @Column(name="is_active", nullable=false)
    private Boolean isActive;
    @Column(name="contract_until")
    private LocalDate contractUntil;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static String $default$squadRole() {
        return "ROTATION";
    }

    @Generated
    private static Boolean $default$isActive() {
        return true;
    }

    @Generated
    public static MdmTeamMembershipBuilder builder() {
        return new MdmTeamMembershipBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public MdmPlayer getPlayer() {
        return this.player;
    }

    @Generated
    public MdmTeam getTeam() {
        return this.team;
    }

    @Generated
    public String getMembershipType() {
        return this.membershipType;
    }

    @Generated
    public Integer getJerseyNumber() {
        return this.jerseyNumber;
    }

    @Generated
    public String getSquadRole() {
        return this.squadRole;
    }

    @Generated
    public Boolean getIsActive() {
        return this.isActive;
    }

    @Generated
    public LocalDate getContractUntil() {
        return this.contractUntil;
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
    public void setPlayer(MdmPlayer player) {
        this.player = player;
    }

    @Generated
    public void setTeam(MdmTeam team) {
        this.team = team;
    }

    @Generated
    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    @Generated
    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    @Generated
    public void setSquadRole(String squadRole) {
        this.squadRole = squadRole;
    }

    @Generated
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Generated
    public void setContractUntil(LocalDate contractUntil) {
        this.contractUntil = contractUntil;
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
    public MdmTeamMembership() {
        this.squadRole = MdmTeamMembership.$default$squadRole();
        this.isActive = MdmTeamMembership.$default$isActive();
    }

    @Generated
    public MdmTeamMembership(Long id, MdmPlayer player, MdmTeam team, String membershipType, Integer jerseyNumber, String squadRole, Boolean isActive, LocalDate contractUntil, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.player = player;
        this.team = team;
        this.membershipType = membershipType;
        this.jerseyNumber = jerseyNumber;
        this.squadRole = squadRole;
        this.isActive = isActive;
        this.contractUntil = contractUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmTeamMembershipBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmPlayer player;
        @Generated
        private MdmTeam team;
        @Generated
        private String membershipType;
        @Generated
        private Integer jerseyNumber;
        @Generated
        private boolean squadRole$set;
        @Generated
        private String squadRole$value;
        @Generated
        private boolean isActive$set;
        @Generated
        private Boolean isActive$value;
        @Generated
        private LocalDate contractUntil;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmTeamMembershipBuilder() {
        }

        @Generated
        public MdmTeamMembershipBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmTeamMembershipBuilder player(MdmPlayer player) {
            this.player = player;
            return this;
        }

        @Generated
        public MdmTeamMembershipBuilder team(MdmTeam team) {
            this.team = team;
            return this;
        }

        @Generated
        public MdmTeamMembershipBuilder membershipType(String membershipType) {
            this.membershipType = membershipType;
            return this;
        }

        @Generated
        public MdmTeamMembershipBuilder jerseyNumber(Integer jerseyNumber) {
            this.jerseyNumber = jerseyNumber;
            return this;
        }

        @Generated
        public MdmTeamMembershipBuilder squadRole(String squadRole) {
            this.squadRole$value = squadRole;
            this.squadRole$set = true;
            return this;
        }

        @Generated
        public MdmTeamMembershipBuilder isActive(Boolean isActive) {
            this.isActive$value = isActive;
            this.isActive$set = true;
            return this;
        }

        @Generated
        public MdmTeamMembershipBuilder contractUntil(LocalDate contractUntil) {
            this.contractUntil = contractUntil;
            return this;
        }

        @Generated
        public MdmTeamMembershipBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmTeamMembershipBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmTeamMembership build() {
            String squadRole$value = this.squadRole$value;
            if (!this.squadRole$set) {
                squadRole$value = MdmTeamMembership.$default$squadRole();
            }
            Boolean isActive$value = this.isActive$value;
            if (!this.isActive$set) {
                isActive$value = MdmTeamMembership.$default$isActive();
            }
            return new MdmTeamMembership(this.id, this.player, this.team, this.membershipType, this.jerseyNumber, squadRole$value, isActive$value, this.contractUntil, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmTeamMembership.MdmTeamMembershipBuilder(id=" + this.id + ", player=" + String.valueOf(this.player) + ", team=" + String.valueOf(this.team) + ", membershipType=" + this.membershipType + ", jerseyNumber=" + this.jerseyNumber + ", squadRole$value=" + this.squadRole$value + ", isActive$value=" + this.isActive$value + ", contractUntil=" + String.valueOf(this.contractUntil) + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

