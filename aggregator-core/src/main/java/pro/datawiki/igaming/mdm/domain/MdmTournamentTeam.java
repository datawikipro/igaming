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
 *  jakarta.persistence.Index
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pro.datawiki.igaming.mdm.domain.MdmSport;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTournament;

@Entity
@Table(name="mdm_tournament_team", uniqueConstraints={@UniqueConstraint(name="uk_mdm_tournament_team", columnNames={"tournament_id", "team_id"})}, indexes={@Index(name="idx_mdm_tourn_team_lookup", columnList="tournament_id, team_id"), @Index(name="idx_mdm_team_tourn_lookup", columnList="team_id, tournament_id"), @Index(name="idx_mdm_tourn_team_active", columnList="tournament_id, is_active")})
public class MdmTournamentTeam {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="tournament_id", nullable=false)
    private MdmTournament tournament;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="team_id", nullable=false)
    private MdmTeam team;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="sport_id", nullable=false)
    private MdmSport sport;
    @Column(length=32)
    private String season;
    @Column(name="is_active", nullable=false)
    private Boolean isActive;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static Boolean $default$isActive() {
        return true;
    }

    @Generated
    public static MdmTournamentTeamBuilder builder() {
        return new MdmTournamentTeamBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public MdmTournament getTournament() {
        return this.tournament;
    }

    @Generated
    public MdmTeam getTeam() {
        return this.team;
    }

    @Generated
    public MdmSport getSport() {
        return this.sport;
    }

    @Generated
    public String getSeason() {
        return this.season;
    }

    @Generated
    public Boolean getIsActive() {
        return this.isActive;
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
    public void setTournament(MdmTournament tournament) {
        this.tournament = tournament;
    }

    @Generated
    public void setTeam(MdmTeam team) {
        this.team = team;
    }

    @Generated
    public void setSport(MdmSport sport) {
        this.sport = sport;
    }

    @Generated
    public void setSeason(String season) {
        this.season = season;
    }

    @Generated
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    public MdmTournamentTeam() {
        this.isActive = MdmTournamentTeam.$default$isActive();
    }

    @Generated
    public MdmTournamentTeam(Long id, MdmTournament tournament, MdmTeam team, MdmSport sport, String season, Boolean isActive, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.tournament = tournament;
        this.team = team;
        this.sport = sport;
        this.season = season;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmTournamentTeamBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmTournament tournament;
        @Generated
        private MdmTeam team;
        @Generated
        private MdmSport sport;
        @Generated
        private String season;
        @Generated
        private boolean isActive$set;
        @Generated
        private Boolean isActive$value;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmTournamentTeamBuilder() {
        }

        @Generated
        public MdmTournamentTeamBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmTournamentTeamBuilder tournament(MdmTournament tournament) {
            this.tournament = tournament;
            return this;
        }

        @Generated
        public MdmTournamentTeamBuilder team(MdmTeam team) {
            this.team = team;
            return this;
        }

        @Generated
        public MdmTournamentTeamBuilder sport(MdmSport sport) {
            this.sport = sport;
            return this;
        }

        @Generated
        public MdmTournamentTeamBuilder season(String season) {
            this.season = season;
            return this;
        }

        @Generated
        public MdmTournamentTeamBuilder isActive(Boolean isActive) {
            this.isActive$value = isActive;
            this.isActive$set = true;
            return this;
        }

        @Generated
        public MdmTournamentTeamBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmTournamentTeamBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmTournamentTeam build() {
            Boolean isActive$value = this.isActive$value;
            if (!this.isActive$set) {
                isActive$value = MdmTournamentTeam.$default$isActive();
            }
            return new MdmTournamentTeam(this.id, this.tournament, this.team, this.sport, this.season, isActive$value, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmTournamentTeam.MdmTournamentTeamBuilder(id=" + this.id + ", tournament=" + String.valueOf(this.tournament) + ", team=" + String.valueOf(this.team) + ", sport=" + String.valueOf(this.sport) + ", season=" + this.season + ", isActive$value=" + this.isActive$value + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

