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
import pro.datawiki.igaming.mdm.domain.MdmSeason;
import pro.datawiki.igaming.mdm.domain.MdmSport;

@Entity
@Table(name="mdm_season_stage", uniqueConstraints={@UniqueConstraint(name="uk_mdm_stage_season_slug", columnNames={"season_id", "slug"}), @UniqueConstraint(name="uk_mdm_stage_id_sport", columnNames={"id", "sport_id"})}, indexes={@Index(name="idx_mdm_stage_season", columnList="season_id"), @Index(name="idx_mdm_stage_type", columnList="stage_type")})
public class MdmSeasonStage {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="season_id", nullable=false)
    private MdmSeason season;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="sport_id", nullable=false)
    private MdmSport sport;
    @Column(nullable=false, length=191)
    private String slug;
    @Column(nullable=false)
    private String name;
    @Column(name="stage_type", nullable=false, length=64)
    private String stageType;
    @Column(name="default_best_of", nullable=false)
    private Integer defaultBestOf;
    @Column(name="stage_order", nullable=false)
    private Integer stageOrder;
    @Column(name="is_playoff", nullable=false)
    private Boolean isPlayoff;
    @Column(name="is_grand_final", nullable=false)
    private Boolean isGrandFinal;
    @Column(name="bracket_meta_json", columnDefinition="jsonb")
    private String bracketMetaJson;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Generated
    private static String $default$stageType() {
        return "SINGLE_ELIMINATION";
    }

    @Generated
    private static Integer $default$defaultBestOf() {
        return 3;
    }

    @Generated
    private static Integer $default$stageOrder() {
        return 1;
    }

    @Generated
    private static Boolean $default$isPlayoff() {
        return false;
    }

    @Generated
    private static Boolean $default$isGrandFinal() {
        return false;
    }

    @Generated
    public static MdmSeasonStageBuilder builder() {
        return new MdmSeasonStageBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public MdmSeason getSeason() {
        return this.season;
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
    public String getStageType() {
        return this.stageType;
    }

    @Generated
    public Integer getDefaultBestOf() {
        return this.defaultBestOf;
    }

    @Generated
    public Integer getStageOrder() {
        return this.stageOrder;
    }

    @Generated
    public Boolean getIsPlayoff() {
        return this.isPlayoff;
    }

    @Generated
    public Boolean getIsGrandFinal() {
        return this.isGrandFinal;
    }

    @Generated
    public String getBracketMetaJson() {
        return this.bracketMetaJson;
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
    public void setSeason(MdmSeason season) {
        this.season = season;
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
    public void setStageType(String stageType) {
        this.stageType = stageType;
    }

    @Generated
    public void setDefaultBestOf(Integer defaultBestOf) {
        this.defaultBestOf = defaultBestOf;
    }

    @Generated
    public void setStageOrder(Integer stageOrder) {
        this.stageOrder = stageOrder;
    }

    @Generated
    public void setIsPlayoff(Boolean isPlayoff) {
        this.isPlayoff = isPlayoff;
    }

    @Generated
    public void setIsGrandFinal(Boolean isGrandFinal) {
        this.isGrandFinal = isGrandFinal;
    }

    @Generated
    public void setBracketMetaJson(String bracketMetaJson) {
        this.bracketMetaJson = bracketMetaJson;
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
    public MdmSeasonStage() {
        this.stageType = MdmSeasonStage.$default$stageType();
        this.defaultBestOf = MdmSeasonStage.$default$defaultBestOf();
        this.stageOrder = MdmSeasonStage.$default$stageOrder();
        this.isPlayoff = MdmSeasonStage.$default$isPlayoff();
        this.isGrandFinal = MdmSeasonStage.$default$isGrandFinal();
    }

    @Generated
    public MdmSeasonStage(Long id, MdmSeason season, MdmSport sport, String slug, String name, String stageType, Integer defaultBestOf, Integer stageOrder, Boolean isPlayoff, Boolean isGrandFinal, String bracketMetaJson, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.season = season;
        this.sport = sport;
        this.slug = slug;
        this.name = name;
        this.stageType = stageType;
        this.defaultBestOf = defaultBestOf;
        this.stageOrder = stageOrder;
        this.isPlayoff = isPlayoff;
        this.isGrandFinal = isGrandFinal;
        this.bracketMetaJson = bracketMetaJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmSeasonStageBuilder {
        @Generated
        private Long id;
        @Generated
        private MdmSeason season;
        @Generated
        private MdmSport sport;
        @Generated
        private String slug;
        @Generated
        private String name;
        @Generated
        private boolean stageType$set;
        @Generated
        private String stageType$value;
        @Generated
        private boolean defaultBestOf$set;
        @Generated
        private Integer defaultBestOf$value;
        @Generated
        private boolean stageOrder$set;
        @Generated
        private Integer stageOrder$value;
        @Generated
        private boolean isPlayoff$set;
        @Generated
        private Boolean isPlayoff$value;
        @Generated
        private boolean isGrandFinal$set;
        @Generated
        private Boolean isGrandFinal$value;
        @Generated
        private String bracketMetaJson;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmSeasonStageBuilder() {
        }

        @Generated
        public MdmSeasonStageBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder season(MdmSeason season) {
            this.season = season;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder sport(MdmSport sport) {
            this.sport = sport;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder stageType(String stageType) {
            this.stageType$value = stageType;
            this.stageType$set = true;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder defaultBestOf(Integer defaultBestOf) {
            this.defaultBestOf$value = defaultBestOf;
            this.defaultBestOf$set = true;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder stageOrder(Integer stageOrder) {
            this.stageOrder$value = stageOrder;
            this.stageOrder$set = true;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder isPlayoff(Boolean isPlayoff) {
            this.isPlayoff$value = isPlayoff;
            this.isPlayoff$set = true;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder isGrandFinal(Boolean isGrandFinal) {
            this.isGrandFinal$value = isGrandFinal;
            this.isGrandFinal$set = true;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder bracketMetaJson(String bracketMetaJson) {
            this.bracketMetaJson = bracketMetaJson;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmSeasonStageBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmSeasonStage build() {
            String stageType$value = this.stageType$value;
            if (!this.stageType$set) {
                stageType$value = MdmSeasonStage.$default$stageType();
            }
            Integer defaultBestOf$value = this.defaultBestOf$value;
            if (!this.defaultBestOf$set) {
                defaultBestOf$value = MdmSeasonStage.$default$defaultBestOf();
            }
            Integer stageOrder$value = this.stageOrder$value;
            if (!this.stageOrder$set) {
                stageOrder$value = MdmSeasonStage.$default$stageOrder();
            }
            Boolean isPlayoff$value = this.isPlayoff$value;
            if (!this.isPlayoff$set) {
                isPlayoff$value = MdmSeasonStage.$default$isPlayoff();
            }
            Boolean isGrandFinal$value = this.isGrandFinal$value;
            if (!this.isGrandFinal$set) {
                isGrandFinal$value = MdmSeasonStage.$default$isGrandFinal();
            }
            return new MdmSeasonStage(this.id, this.season, this.sport, this.slug, this.name, stageType$value, defaultBestOf$value, stageOrder$value, isPlayoff$value, isGrandFinal$value, this.bracketMetaJson, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmSeasonStage.MdmSeasonStageBuilder(id=" + this.id + ", season=" + String.valueOf(this.season) + ", sport=" + String.valueOf(this.sport) + ", slug=" + this.slug + ", name=" + this.name + ", stageType$value=" + this.stageType$value + ", defaultBestOf$value=" + this.defaultBestOf$value + ", stageOrder$value=" + this.stageOrder$value + ", isPlayoff$value=" + this.isPlayoff$value + ", isGrandFinal$value=" + this.isGrandFinal$value + ", bracketMetaJson=" + this.bracketMetaJson + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

