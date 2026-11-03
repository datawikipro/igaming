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
import org.hibernate.annotations.UpdateTimestamp;
import pro.datawiki.igaming.mdm.domain.MdmTeam;

@Entity
@Table(name="mdm_team_sofa_metadata")
public class MdmTeamSofaMetadata {
    @Id
    @Column(name="team_id")
    private Long teamId;
    @OneToOne(fetch=FetchType.LAZY)
    @MapsId
    @JoinColumn(name="team_id")
    private MdmTeam team;
    @Column(name="coach_name", length=128)
    private String coachName;
    @Column(name="venue_name", length=191)
    private String venueName;
    @Column(name="venue_city", length=128)
    private String venueCity;
    @Column(name="venue_capacity")
    private Integer venueCapacity;
    @Column(name="sofascore_rating", precision=4, scale=2)
    private BigDecimal sofascoreRating;
    @Column(name="form_recent", length=16)
    private String formRecent;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    public static MdmTeamSofaMetadataBuilder builder() {
        return new MdmTeamSofaMetadataBuilder();
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
    public String getCoachName() {
        return this.coachName;
    }

    @Generated
    public String getVenueName() {
        return this.venueName;
    }

    @Generated
    public String getVenueCity() {
        return this.venueCity;
    }

    @Generated
    public Integer getVenueCapacity() {
        return this.venueCapacity;
    }

    @Generated
    public BigDecimal getSofascoreRating() {
        return this.sofascoreRating;
    }

    @Generated
    public String getFormRecent() {
        return this.formRecent;
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
    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }

    @Generated
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    @Generated
    public void setVenueCity(String venueCity) {
        this.venueCity = venueCity;
    }

    @Generated
    public void setVenueCapacity(Integer venueCapacity) {
        this.venueCapacity = venueCapacity;
    }

    @Generated
    public void setSofascoreRating(BigDecimal sofascoreRating) {
        this.sofascoreRating = sofascoreRating;
    }

    @Generated
    public void setFormRecent(String formRecent) {
        this.formRecent = formRecent;
    }

    @Generated
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Generated
    public MdmTeamSofaMetadata() {
    }

    @Generated
    public MdmTeamSofaMetadata(Long teamId, MdmTeam team, String coachName, String venueName, String venueCity, Integer venueCapacity, BigDecimal sofascoreRating, String formRecent, OffsetDateTime updatedAt) {
        this.teamId = teamId;
        this.team = team;
        this.coachName = coachName;
        this.venueName = venueName;
        this.venueCity = venueCity;
        this.venueCapacity = venueCapacity;
        this.sofascoreRating = sofascoreRating;
        this.formRecent = formRecent;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmTeamSofaMetadataBuilder {
        @Generated
        private Long teamId;
        @Generated
        private MdmTeam team;
        @Generated
        private String coachName;
        @Generated
        private String venueName;
        @Generated
        private String venueCity;
        @Generated
        private Integer venueCapacity;
        @Generated
        private BigDecimal sofascoreRating;
        @Generated
        private String formRecent;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmTeamSofaMetadataBuilder() {
        }

        @Generated
        public MdmTeamSofaMetadataBuilder teamId(Long teamId) {
            this.teamId = teamId;
            return this;
        }

        @Generated
        public MdmTeamSofaMetadataBuilder team(MdmTeam team) {
            this.team = team;
            return this;
        }

        @Generated
        public MdmTeamSofaMetadataBuilder coachName(String coachName) {
            this.coachName = coachName;
            return this;
        }

        @Generated
        public MdmTeamSofaMetadataBuilder venueName(String venueName) {
            this.venueName = venueName;
            return this;
        }

        @Generated
        public MdmTeamSofaMetadataBuilder venueCity(String venueCity) {
            this.venueCity = venueCity;
            return this;
        }

        @Generated
        public MdmTeamSofaMetadataBuilder venueCapacity(Integer venueCapacity) {
            this.venueCapacity = venueCapacity;
            return this;
        }

        @Generated
        public MdmTeamSofaMetadataBuilder sofascoreRating(BigDecimal sofascoreRating) {
            this.sofascoreRating = sofascoreRating;
            return this;
        }

        @Generated
        public MdmTeamSofaMetadataBuilder formRecent(String formRecent) {
            this.formRecent = formRecent;
            return this;
        }

        @Generated
        public MdmTeamSofaMetadataBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmTeamSofaMetadata build() {
            return new MdmTeamSofaMetadata(this.teamId, this.team, this.coachName, this.venueName, this.venueCity, this.venueCapacity, this.sofascoreRating, this.formRecent, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmTeamSofaMetadata.MdmTeamSofaMetadataBuilder(teamId=" + this.teamId + ", team=" + String.valueOf(this.team) + ", coachName=" + this.coachName + ", venueName=" + this.venueName + ", venueCity=" + this.venueCity + ", venueCapacity=" + this.venueCapacity + ", sofascoreRating=" + String.valueOf(this.sofascoreRating) + ", formRecent=" + this.formRecent + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

