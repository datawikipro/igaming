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
import pro.datawiki.igaming.mdm.domain.MdmCategory;
import pro.datawiki.igaming.mdm.domain.MdmCountry;
import pro.datawiki.igaming.mdm.domain.MdmSport;

@Entity
@Table(name="mdm_player", uniqueConstraints={@UniqueConstraint(name="uk_mdm_player_sport_slug", columnNames={"sport_id", "slug"})})
public class MdmPlayer {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="sport_id", nullable=false)
    private MdmSport sport;
    @Column(nullable=false, length=191)
    private String slug;
    @Column(name="sofascore_id", unique=true)
    private Long sofascoreId;
    @Column(name="first_name", length=128)
    private String firstName;
    @Column(name="last_name", length=128)
    private String lastName;
    @Column(name="common_name", nullable=false, length=191)
    private String commonName;
    @Column(name="birth_date")
    private LocalDate birthDate;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="citizenship_category_id")
    private MdmCategory citizenshipCategory;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nationality_country_id")
    private MdmCountry nationality;
    @Column(name="primary_position", length=32)
    private String primaryPosition;
    @Column(name="photo_url", length=512)
    private String photoUrl;
    @Column(nullable=false, length=16)
    private String gender;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static String $default$gender() {
        return "MALE";
    }

    @Generated
    public static MdmPlayerBuilder builder() {
        return new MdmPlayerBuilder();
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
    public String getSlug() {
        return this.slug;
    }

    @Generated
    public Long getSofascoreId() {
        return this.sofascoreId;
    }

    @Generated
    public String getFirstName() {
        return this.firstName;
    }

    @Generated
    public String getLastName() {
        return this.lastName;
    }

    @Generated
    public String getCommonName() {
        return this.commonName;
    }

    @Generated
    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    @Generated
    public MdmCategory getCitizenshipCategory() {
        return this.citizenshipCategory;
    }

    @Generated
    public MdmCountry getNationality() {
        return this.nationality;
    }

    @Generated
    public String getPrimaryPosition() {
        return this.primaryPosition;
    }

    @Generated
    public String getPhotoUrl() {
        return this.photoUrl;
    }

    @Generated
    public String getGender() {
        return this.gender;
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
    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Generated
    public void setSofascoreId(Long sofascoreId) {
        this.sofascoreId = sofascoreId;
    }

    @Generated
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Generated
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Generated
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    @Generated
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @Generated
    public void setCitizenshipCategory(MdmCategory citizenshipCategory) {
        this.citizenshipCategory = citizenshipCategory;
    }

    @Generated
    public void setNationality(MdmCountry nationality) {
        this.nationality = nationality;
    }

    @Generated
    public void setPrimaryPosition(String primaryPosition) {
        this.primaryPosition = primaryPosition;
    }

    @Generated
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Generated
    public void setGender(String gender) {
        this.gender = gender;
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
    public MdmPlayer() {
        this.gender = MdmPlayer.$default$gender();
    }

    @Generated
    public MdmPlayer(Long id, MdmSport sport, String slug, Long sofascoreId, String firstName, String lastName, String commonName, LocalDate birthDate, MdmCategory citizenshipCategory, MdmCountry nationality, String primaryPosition, String photoUrl, String gender, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.sport = sport;
        this.slug = slug;
        this.sofascoreId = sofascoreId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.commonName = commonName;
        this.birthDate = birthDate;
        this.citizenshipCategory = citizenshipCategory;
        this.nationality = nationality;
        this.primaryPosition = primaryPosition;
        this.photoUrl = photoUrl;
        this.gender = gender;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmPlayerBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmSport sport;
        @Generated
        private String slug;
        @Generated
        private Long sofascoreId;
        @Generated
        private String firstName;
        @Generated
        private String lastName;
        @Generated
        private String commonName;
        @Generated
        private LocalDate birthDate;
        @Generated
        private MdmCategory citizenshipCategory;
        @Generated
        private MdmCountry nationality;
        @Generated
        private String primaryPosition;
        @Generated
        private String photoUrl;
        @Generated
        private boolean gender$set;
        @Generated
        private String gender$value;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmPlayerBuilder() {
        }

        @Generated
        public MdmPlayerBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmPlayerBuilder sport(MdmSport sport) {
            this.sport = sport;
            return this;
        }

        @Generated
        public MdmPlayerBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public MdmPlayerBuilder sofascoreId(Long sofascoreId) {
            this.sofascoreId = sofascoreId;
            return this;
        }

        @Generated
        public MdmPlayerBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        @Generated
        public MdmPlayerBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        @Generated
        public MdmPlayerBuilder commonName(String commonName) {
            this.commonName = commonName;
            return this;
        }

        @Generated
        public MdmPlayerBuilder birthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        @Generated
        public MdmPlayerBuilder citizenshipCategory(MdmCategory citizenshipCategory) {
            this.citizenshipCategory = citizenshipCategory;
            return this;
        }

        @Generated
        public MdmPlayerBuilder nationality(MdmCountry nationality) {
            this.nationality = nationality;
            return this;
        }

        @Generated
        public MdmPlayerBuilder primaryPosition(String primaryPosition) {
            this.primaryPosition = primaryPosition;
            return this;
        }

        @Generated
        public MdmPlayerBuilder photoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
            return this;
        }

        @Generated
        public MdmPlayerBuilder gender(String gender) {
            this.gender$value = gender;
            this.gender$set = true;
            return this;
        }

        @Generated
        public MdmPlayerBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmPlayerBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmPlayer build() {
            String gender$value = this.gender$value;
            if (!this.gender$set) {
                gender$value = MdmPlayer.$default$gender();
            }
            return new MdmPlayer(this.id, this.sport, this.slug, this.sofascoreId, this.firstName, this.lastName, this.commonName, this.birthDate, this.citizenshipCategory, this.nationality, this.primaryPosition, this.photoUrl, gender$value, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmPlayer.MdmPlayerBuilder(id=" + this.id + ", sport=" + String.valueOf(this.sport) + ", slug=" + this.slug + ", sofascoreId=" + this.sofascoreId + ", firstName=" + this.firstName + ", lastName=" + this.lastName + ", commonName=" + this.commonName + ", birthDate=" + String.valueOf(this.birthDate) + ", citizenshipCategory=" + String.valueOf(this.citizenshipCategory) + ", nationality=" + String.valueOf(this.nationality) + ", primaryPosition=" + this.primaryPosition + ", photoUrl=" + this.photoUrl + ", gender$value=" + this.gender$value + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

