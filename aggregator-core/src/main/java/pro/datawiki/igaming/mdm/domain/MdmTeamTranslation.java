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
import pro.datawiki.igaming.mdm.domain.MdmTeam;

@Entity
@Table(name="mdm_team_translation", uniqueConstraints={@UniqueConstraint(name="uk_mdm_team_trans", columnNames={"team_id", "lang"})})
public class MdmTeamTranslation {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="team_id", nullable=false)
    private MdmTeam team;
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
    public static MdmTeamTranslationBuilder builder() {
        return new MdmTeamTranslationBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public MdmTeam getTeam() {
        return this.team;
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
    public void setTeam(MdmTeam team) {
        this.team = team;
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
    public MdmTeamTranslation() {
    }

    @Generated
    public MdmTeamTranslation(Long id, MdmTeam team, String lang, String name, String shortName, OffsetDateTime createdAt) {
        this.id = id;
        this.team = team;
        this.lang = lang;
        this.name = name;
        this.shortName = shortName;
        this.createdAt = createdAt;
    }

    @Generated
    public static class MdmTeamTranslationBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmTeam team;
        @Generated
        private String lang;
        @Generated
        private String name;
        @Generated
        private String shortName;
        @Generated
        private OffsetDateTime createdAt;

        @Generated
        MdmTeamTranslationBuilder() {
        }

        @Generated
        public MdmTeamTranslationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmTeamTranslationBuilder team(MdmTeam team) {
            this.team = team;
            return this;
        }

        @Generated
        public MdmTeamTranslationBuilder lang(String lang) {
            this.lang = lang;
            return this;
        }

        @Generated
        public MdmTeamTranslationBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Generated
        public MdmTeamTranslationBuilder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        @Generated
        public MdmTeamTranslationBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmTeamTranslation build() {
            return new MdmTeamTranslation(this.id, this.team, this.lang, this.name, this.shortName, this.createdAt);
        }

        @Generated
        public String toString() {
            return "MdmTeamTranslation.MdmTeamTranslationBuilder(id=" + this.id + ", team=" + String.valueOf(this.team) + ", lang=" + this.lang + ", name=" + this.name + ", shortName=" + this.shortName + ", createdAt=" + String.valueOf(this.createdAt) + ")";
        }
    }
}

