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
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pro.datawiki.igaming.mdm.domain.MdmSeasonStage;
import pro.datawiki.igaming.mdm.domain.MdmSport;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTournament;

@Entity
@Table(name="mdm_match", indexes={@Index(name="idx_mdm_match_sofascore", columnList="sofascore_id"), @Index(name="idx_mdm_match_teams_time", columnList="sport_id, home_team_id, away_team_id, start_time"), @Index(name="idx_mdm_match_reverse_teams_time", columnList="sport_id, away_team_id, home_team_id, start_time"), @Index(name="idx_mdm_match_active", columnList="status, start_time"), @Index(name="idx_mdm_match_tourn_time", columnList="tournament_id, start_time")})
public class MdmMatch {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="sport_id", nullable=false)
    private MdmSport sport;
    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="tournament_id", nullable=false)
    private MdmTournament tournament;
    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="home_team_id", nullable=false)
    private MdmTeam homeTeam;
    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="away_team_id", nullable=false)
    private MdmTeam awayTeam;
    @Column(name="start_time", nullable=false)
    private OffsetDateTime startTime;
    @Column(name="sofascore_id", unique=true)
    private Long sofascoreId;
    @Column(name="source_origin", nullable=false, length=32)
    private String sourceOrigin;
    @Column(name="created_by_source", nullable=false, length=64)
    private String createdBySource;
    @Column(name="status", nullable=false, length=32)
    private String status;
    @Column(name="score1", length=16)
    private String score1;
    @Column(name="score2", length=16)
    private String score2;
    @Column(name="regular_score1", length=16)
    private String regularScore1;
    @Column(name="regular_score2", length=16)
    private String regularScore2;
    @Column(name="overtime_score1", length=16)
    private String overtimeScore1;
    @Column(name="overtime_score2", length=16)
    private String overtimeScore2;
    @Column(name="penalties_score1", length=16)
    private String penaltiesScore1;
    @Column(name="penalties_score2", length=16)
    private String penaltiesScore2;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stage_id")
    private MdmSeasonStage stageEntity;
    @Column(name="best_of", nullable=false)
    private Integer bestOf;
    @Column(name="stage", length=64)
    private String stage;
    @Column(name="round", length=64)
    private String round;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static String $default$sourceOrigin() {
        return "SOFASCORE";
    }

    @Generated
    private static String $default$status() {
        return "SCHEDULED";
    }

    @Generated
    private static Integer $default$bestOf() {
        return 3;
    }

    @Generated
    public static MdmMatchBuilder builder() {
        return new MdmMatchBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public MdmSport getSport() {
        return this.sport;
    }

    @Generated
    public MdmTournament getTournament() {
        return this.tournament;
    }

    @Generated
    public MdmTeam getHomeTeam() {
        return this.homeTeam;
    }

    @Generated
    public MdmTeam getAwayTeam() {
        return this.awayTeam;
    }

    @Generated
    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    @Generated
    public Long getSofascoreId() {
        return this.sofascoreId;
    }

    @Generated
    public String getSourceOrigin() {
        return this.sourceOrigin;
    }

    @Generated
    public String getCreatedBySource() {
        return this.createdBySource;
    }

    @Generated
    public String getStatus() {
        return this.status;
    }

    @Generated
    public String getScore1() {
        return this.score1;
    }

    @Generated
    public String getScore2() {
        return this.score2;
    }

    @Generated
    public String getRegularScore1() {
        return this.regularScore1;
    }

    @Generated
    public String getRegularScore2() {
        return this.regularScore2;
    }

    @Generated
    public String getOvertimeScore1() {
        return this.overtimeScore1;
    }

    @Generated
    public String getOvertimeScore2() {
        return this.overtimeScore2;
    }

    @Generated
    public String getPenaltiesScore1() {
        return this.penaltiesScore1;
    }

    @Generated
    public String getPenaltiesScore2() {
        return this.penaltiesScore2;
    }

    @Generated
    public MdmSeasonStage getStageEntity() {
        return this.stageEntity;
    }

    @Generated
    public Integer getBestOf() {
        return this.bestOf;
    }

    @Generated
    public String getStage() {
        return this.stage;
    }

    @Generated
    public String getRound() {
        return this.round;
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
    public void setSport(MdmSport sport) {
        this.sport = sport;
    }

    @Generated
    public void setTournament(MdmTournament tournament) {
        this.tournament = tournament;
    }

    @Generated
    public void setHomeTeam(MdmTeam homeTeam) {
        this.homeTeam = homeTeam;
    }

    @Generated
    public void setAwayTeam(MdmTeam awayTeam) {
        this.awayTeam = awayTeam;
    }

    @Generated
    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    @Generated
    public void setSofascoreId(Long sofascoreId) {
        this.sofascoreId = sofascoreId;
    }

    @Generated
    public void setSourceOrigin(String sourceOrigin) {
        this.sourceOrigin = sourceOrigin;
    }

    @Generated
    public void setCreatedBySource(String createdBySource) {
        this.createdBySource = createdBySource;
    }

    @Generated
    public void setStatus(String status) {
        this.status = status;
    }

    @Generated
    public void setScore1(String score1) {
        this.score1 = score1;
    }

    @Generated
    public void setScore2(String score2) {
        this.score2 = score2;
    }

    @Generated
    public void setRegularScore1(String regularScore1) {
        this.regularScore1 = regularScore1;
    }

    @Generated
    public void setRegularScore2(String regularScore2) {
        this.regularScore2 = regularScore2;
    }

    @Generated
    public void setOvertimeScore1(String overtimeScore1) {
        this.overtimeScore1 = overtimeScore1;
    }

    @Generated
    public void setOvertimeScore2(String overtimeScore2) {
        this.overtimeScore2 = overtimeScore2;
    }

    @Generated
    public void setPenaltiesScore1(String penaltiesScore1) {
        this.penaltiesScore1 = penaltiesScore1;
    }

    @Generated
    public void setPenaltiesScore2(String penaltiesScore2) {
        this.penaltiesScore2 = penaltiesScore2;
    }

    @Generated
    public void setStageEntity(MdmSeasonStage stageEntity) {
        this.stageEntity = stageEntity;
    }

    @Generated
    public void setBestOf(Integer bestOf) {
        this.bestOf = bestOf;
    }

    @Generated
    public void setStage(String stage) {
        this.stage = stage;
    }

    @Generated
    public void setRound(String round) {
        this.round = round;
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
    public MdmMatch() {
        this.sourceOrigin = MdmMatch.$default$sourceOrigin();
        this.status = MdmMatch.$default$status();
        this.bestOf = MdmMatch.$default$bestOf();
    }

    @Generated
    public MdmMatch(Long id, MdmSport sport, MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, OffsetDateTime startTime, Long sofascoreId, String sourceOrigin, String createdBySource, String status, String score1, String score2, String regularScore1, String regularScore2, String overtimeScore1, String overtimeScore2, String penaltiesScore1, String penaltiesScore2, MdmSeasonStage stageEntity, Integer bestOf, String stage, String round, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.sport = sport;
        this.tournament = tournament;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.startTime = startTime;
        this.sofascoreId = sofascoreId;
        this.sourceOrigin = sourceOrigin;
        this.createdBySource = createdBySource;
        this.status = status;
        this.score1 = score1;
        this.score2 = score2;
        this.regularScore1 = regularScore1;
        this.regularScore2 = regularScore2;
        this.overtimeScore1 = overtimeScore1;
        this.overtimeScore2 = overtimeScore2;
        this.penaltiesScore1 = penaltiesScore1;
        this.penaltiesScore2 = penaltiesScore2;
        this.stageEntity = stageEntity;
        this.bestOf = bestOf;
        this.stage = stage;
        this.round = round;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmMatchBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmSport sport;
        @Generated
        private MdmTournament tournament;
        @Generated
        private MdmTeam homeTeam;
        @Generated
        private MdmTeam awayTeam;
        @Generated
        private OffsetDateTime startTime;
        @Generated
        private Long sofascoreId;
        @Generated
        private boolean sourceOrigin$set;
        @Generated
        private String sourceOrigin$value;
        @Generated
        private String createdBySource;
        @Generated
        private boolean status$set;
        @Generated
        private String status$value;
        @Generated
        private String score1;
        @Generated
        private String score2;
        @Generated
        private String regularScore1;
        @Generated
        private String regularScore2;
        @Generated
        private String overtimeScore1;
        @Generated
        private String overtimeScore2;
        @Generated
        private String penaltiesScore1;
        @Generated
        private String penaltiesScore2;
        @Generated
        private MdmSeasonStage stageEntity;
        @Generated
        private boolean bestOf$set;
        @Generated
        private Integer bestOf$value;
        @Generated
        private String stage;
        @Generated
        private String round;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmMatchBuilder() {
        }

        @Generated
        public MdmMatchBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmMatchBuilder sport(MdmSport sport) {
            this.sport = sport;
            return this;
        }

        @Generated
        public MdmMatchBuilder tournament(MdmTournament tournament) {
            this.tournament = tournament;
            return this;
        }

        @Generated
        public MdmMatchBuilder homeTeam(MdmTeam homeTeam) {
            this.homeTeam = homeTeam;
            return this;
        }

        @Generated
        public MdmMatchBuilder awayTeam(MdmTeam awayTeam) {
            this.awayTeam = awayTeam;
            return this;
        }

        @Generated
        public MdmMatchBuilder startTime(OffsetDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        @Generated
        public MdmMatchBuilder sofascoreId(Long sofascoreId) {
            this.sofascoreId = sofascoreId;
            return this;
        }

        @Generated
        public MdmMatchBuilder sourceOrigin(String sourceOrigin) {
            this.sourceOrigin$value = sourceOrigin;
            this.sourceOrigin$set = true;
            return this;
        }

        @Generated
        public MdmMatchBuilder createdBySource(String createdBySource) {
            this.createdBySource = createdBySource;
            return this;
        }

        @Generated
        public MdmMatchBuilder status(String status) {
            this.status$value = status;
            this.status$set = true;
            return this;
        }

        @Generated
        public MdmMatchBuilder score1(String score1) {
            this.score1 = score1;
            return this;
        }

        @Generated
        public MdmMatchBuilder score2(String score2) {
            this.score2 = score2;
            return this;
        }

        @Generated
        public MdmMatchBuilder regularScore1(String regularScore1) {
            this.regularScore1 = regularScore1;
            return this;
        }

        @Generated
        public MdmMatchBuilder regularScore2(String regularScore2) {
            this.regularScore2 = regularScore2;
            return this;
        }

        @Generated
        public MdmMatchBuilder overtimeScore1(String overtimeScore1) {
            this.overtimeScore1 = overtimeScore1;
            return this;
        }

        @Generated
        public MdmMatchBuilder overtimeScore2(String overtimeScore2) {
            this.overtimeScore2 = overtimeScore2;
            return this;
        }

        @Generated
        public MdmMatchBuilder penaltiesScore1(String penaltiesScore1) {
            this.penaltiesScore1 = penaltiesScore1;
            return this;
        }

        @Generated
        public MdmMatchBuilder penaltiesScore2(String penaltiesScore2) {
            this.penaltiesScore2 = penaltiesScore2;
            return this;
        }

        @Generated
        public MdmMatchBuilder stageEntity(MdmSeasonStage stageEntity) {
            this.stageEntity = stageEntity;
            return this;
        }

        @Generated
        public MdmMatchBuilder bestOf(Integer bestOf) {
            this.bestOf$value = bestOf;
            this.bestOf$set = true;
            return this;
        }

        @Generated
        public MdmMatchBuilder stage(String stage) {
            this.stage = stage;
            return this;
        }

        @Generated
        public MdmMatchBuilder round(String round) {
            this.round = round;
            return this;
        }

        @Generated
        public MdmMatchBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmMatchBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmMatch build() {
            String sourceOrigin$value = this.sourceOrigin$value;
            if (!this.sourceOrigin$set) {
                sourceOrigin$value = MdmMatch.$default$sourceOrigin();
            }
            String status$value = this.status$value;
            if (!this.status$set) {
                status$value = MdmMatch.$default$status();
            }
            Integer bestOf$value = this.bestOf$value;
            if (!this.bestOf$set) {
                bestOf$value = MdmMatch.$default$bestOf();
            }
            return new MdmMatch(this.id, this.sport, this.tournament, this.homeTeam, this.awayTeam, this.startTime, this.sofascoreId, sourceOrigin$value, this.createdBySource, status$value, this.score1, this.score2, this.regularScore1, this.regularScore2, this.overtimeScore1, this.overtimeScore2, this.penaltiesScore1, this.penaltiesScore2, this.stageEntity, bestOf$value, this.stage, this.round, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmMatch.MdmMatchBuilder(id=" + this.id + ", sport=" + String.valueOf(this.sport) + ", tournament=" + String.valueOf(this.tournament) + ", homeTeam=" + String.valueOf(this.homeTeam) + ", awayTeam=" + String.valueOf(this.awayTeam) + ", startTime=" + String.valueOf(this.startTime) + ", sofascoreId=" + this.sofascoreId + ", sourceOrigin$value=" + this.sourceOrigin$value + ", createdBySource=" + this.createdBySource + ", status$value=" + this.status$value + ", score1=" + this.score1 + ", score2=" + this.score2 + ", regularScore1=" + this.regularScore1 + ", regularScore2=" + this.regularScore2 + ", overtimeScore1=" + this.overtimeScore1 + ", overtimeScore2=" + this.overtimeScore2 + ", penaltiesScore1=" + this.penaltiesScore1 + ", penaltiesScore2=" + this.penaltiesScore2 + ", stageEntity=" + String.valueOf(this.stageEntity) + ", bestOf$value=" + this.bestOf$value + ", stage=" + this.stage + ", round=" + this.round + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

