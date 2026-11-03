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
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;
import pro.datawiki.igaming.mdm.domain.MdmTournament;

@Entity
@Table(name="mdm_tournament_translation", uniqueConstraints={@UniqueConstraint(name="uk_mdm_tourn_trans", columnNames={"tournament_id", "lang"})})
public class MdmTournamentTranslation {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="tournament_id", nullable=false)
    private MdmTournament tournament;
    @Column(nullable=false, length=8)
    private String lang;
    @Column(nullable=false)
    private String name;
    @Column(name="short_name", length=64)
    private String shortName;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Generated
    public static MdmTournamentTranslationBuilder builder() {
        return new MdmTournamentTranslationBuilder();
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
    public String getLang() {
        return this.lang;
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getShortName() {
        return this.shortName;
    }

    @Generated
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
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
    public void setLang(String lang) {
        this.lang = lang;
    }

    @Generated
    public void setName(String name) {
        this.name = name;
    }

    @Generated
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Generated
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Generated
    public MdmTournamentTranslation() {
    }

    @Generated
    public MdmTournamentTranslation(Long id, MdmTournament tournament, String lang, String name, String shortName, OffsetDateTime createdAt) {
        this.id = id;
        this.tournament = tournament;
        this.lang = lang;
        this.name = name;
        this.shortName = shortName;
        this.createdAt = createdAt;
    }

    @Generated
    public static class MdmTournamentTranslationBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmTournament tournament;
        @Generated
        private String lang;
        @Generated
        private String name;
        @Generated
        private String shortName;
        @Generated
        private OffsetDateTime createdAt;

        @Generated
        MdmTournamentTranslationBuilder() {
        }

        @Generated
        public MdmTournamentTranslationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmTournamentTranslationBuilder tournament(MdmTournament tournament) {
            this.tournament = tournament;
            return this;
        }

        @Generated
        public MdmTournamentTranslationBuilder lang(String lang) {
            this.lang = lang;
            return this;
        }

        @Generated
        public MdmTournamentTranslationBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Generated
        public MdmTournamentTranslationBuilder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        @Generated
        public MdmTournamentTranslationBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmTournamentTranslation build() {
            return new MdmTournamentTranslation(this.id, this.tournament, this.lang, this.name, this.shortName, this.createdAt);
        }

        @Generated
        public String toString() {
            return "MdmTournamentTranslation.MdmTournamentTranslationBuilder(id=" + this.id + ", tournament=" + String.valueOf(this.tournament) + ", lang=" + this.lang + ", name=" + this.name + ", shortName=" + this.shortName + ", createdAt=" + String.valueOf(this.createdAt) + ")";
        }
    }
}

