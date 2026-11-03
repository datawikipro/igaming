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
import pro.datawiki.igaming.mdm.domain.MdmTournament;

@Entity
@Table(name="mdm_season", uniqueConstraints={@UniqueConstraint(name="uk_mdm_season_tourn_slug", columnNames={"tournament_id", "slug"}), @UniqueConstraint(name="uk_mdm_season_id_sport", columnNames={"id", "sport_id"})}, indexes={@Index(name="idx_mdm_season_tourn", columnList="tournament_id"), @Index(name="idx_mdm_season_active", columnList="status")})
public class MdmSeason {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="tournament_id", nullable=false)
    private MdmTournament tournament;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="sport_id", nullable=false)
    private MdmSport sport;
    @Column(nullable=false, length=191)
    private String slug;
    @Column(nullable=false)
    private String name;
    @Column
    private Integer year;
    @Column(name="start_date")
    private OffsetDateTime startDate;
    @Column(name="end_date")
    private OffsetDateTime endDate;
    @Column(nullable=false, length=32)
    private String status;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static String $default$status() {
        return "ACTIVE";
    }

    @Generated
    public static MdmSeasonBuilder builder() {
        return new MdmSeasonBuilder();
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
    public MdmSport getSport() {
        return this.sport;
    }

    @Generated
    public String getSlug() {
        return this.slug;
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public Integer getYear() {
        return this.year;
    }

    @Generated
    public OffsetDateTime getStartDate() {
        return this.startDate;
    }

    @Generated
    public OffsetDateTime getEndDate() {
        return this.endDate;
    }

    @Generated
    public String getStatus() {
        return this.status;
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
    public void setSport(MdmSport sport) {
        this.sport = sport;
    }

    @Generated
    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Generated
    public void setName(String name) {
        this.name = name;
    }

    @Generated
    public void setYear(Integer year) {
        this.year = year;
    }

    @Generated
    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    @Generated
    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    @Generated
    public void setStatus(String status) {
        this.status = status;
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
    public MdmSeason() {
        this.status = MdmSeason.$default$status();
    }

    @Generated
    public MdmSeason(Long id, MdmTournament tournament, MdmSport sport, String slug, String name, Integer year, OffsetDateTime startDate, OffsetDateTime endDate, String status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.tournament = tournament;
        this.sport = sport;
        this.slug = slug;
        this.name = name;
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmSeasonBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmTournament tournament;
        @Generated
        private MdmSport sport;
        @Generated
        private String slug;
        @Generated
        private String name;
        @Generated
        private Integer year;
        @Generated
        private OffsetDateTime startDate;
        @Generated
        private OffsetDateTime endDate;
        @Generated
        private boolean status$set;
        @Generated
        private String status$value;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmSeasonBuilder() {
        }

        @Generated
        public MdmSeasonBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmSeasonBuilder tournament(MdmTournament tournament) {
            this.tournament = tournament;
            return this;
        }

        @Generated
        public MdmSeasonBuilder sport(MdmSport sport) {
            this.sport = sport;
            return this;
        }

        @Generated
        public MdmSeasonBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public MdmSeasonBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Generated
        public MdmSeasonBuilder year(Integer year) {
            this.year = year;
            return this;
        }

        @Generated
        public MdmSeasonBuilder startDate(OffsetDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        @Generated
        public MdmSeasonBuilder endDate(OffsetDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        @Generated
        public MdmSeasonBuilder status(String status) {
            this.status$value = status;
            this.status$set = true;
            return this;
        }

        @Generated
        public MdmSeasonBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmSeasonBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmSeason build() {
            String status$value = this.status$value;
            if (!this.status$set) {
                status$value = MdmSeason.$default$status();
            }
            return new MdmSeason(this.id, this.tournament, this.sport, this.slug, this.name, this.year, this.startDate, this.endDate, status$value, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmSeason.MdmSeasonBuilder(id=" + this.id + ", tournament=" + String.valueOf(this.tournament) + ", sport=" + String.valueOf(this.sport) + ", slug=" + this.slug + ", name=" + this.name + ", year=" + this.year + ", startDate=" + String.valueOf(this.startDate) + ", endDate=" + String.valueOf(this.endDate) + ", status$value=" + this.status$value + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

