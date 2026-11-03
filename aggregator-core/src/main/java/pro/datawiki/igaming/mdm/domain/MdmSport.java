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
 *  lombok.Generated
 *  org.hibernate.annotations.CreationTimestamp
 *  org.hibernate.annotations.UpdateTimestamp
 *  pro.datawiki.igaming.dto.sport.Sport
 *  pro.datawiki.igaming.dto.sport.Sports
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
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.dto.sport.Sports;

@Entity
@Table(name="mdm_sport")
public class MdmSport {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable=false, unique=true, length=64)
    private String slug;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private MdmSport parent;
    @Column(name="domain_key", nullable=false, length=64)
    private String domainKey;
    @Column(name="is_esports", nullable=false)
    private Boolean isEsports;
    @Column(name="is_team_sport", nullable=false)
    private Boolean isTeamSport;
    @Column(name="sort_order", nullable=false)
    private Integer sortOrder;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    public Sport asDomain() {
        return this.slug != null ? Sports.get((String)this.slug) : null;
    }

    @Generated
    private static Boolean $default$isEsports() {
        return false;
    }

    @Generated
    private static Boolean $default$isTeamSport() {
        return true;
    }

    @Generated
    private static Integer $default$sortOrder() {
        return 100;
    }

    @Generated
    public static MdmSportBuilder builder() {
        return new MdmSportBuilder();
    }

    @Generated
    public Integer getId() {
        return this.id;
    }

    @Generated
    public String getSlug() {
        return this.slug;
    }

    @Generated
    public MdmSport getParent() {
        return this.parent;
    }

    @Generated
    public String getDomainKey() {
        return this.domainKey;
    }

    @Generated
    public Boolean getIsEsports() {
        return this.isEsports;
    }

    @Generated
    public Boolean getIsTeamSport() {
        return this.isTeamSport;
    }

    @Generated
    public Integer getSortOrder() {
        return this.sortOrder;
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
    public void setId(Integer id) {
        this.id = id;
    }

    @Generated
    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Generated
    public void setParent(MdmSport parent) {
        this.parent = parent;
    }

    @Generated
    public void setDomainKey(String domainKey) {
        this.domainKey = domainKey;
    }

    @Generated
    public void setIsEsports(Boolean isEsports) {
        this.isEsports = isEsports;
    }

    @Generated
    public void setIsTeamSport(Boolean isTeamSport) {
        this.isTeamSport = isTeamSport;
    }

    @Generated
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
    public MdmSport() {
        this.isEsports = MdmSport.$default$isEsports();
        this.isTeamSport = MdmSport.$default$isTeamSport();
        this.sortOrder = MdmSport.$default$sortOrder();
    }

    @Generated
    public MdmSport(Integer id, String slug, MdmSport parent, String domainKey, Boolean isEsports, Boolean isTeamSport, Integer sortOrder, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.slug = slug;
        this.parent = parent;
        this.domainKey = domainKey;
        this.isEsports = isEsports;
        this.isTeamSport = isTeamSport;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class MdmSportBuilder {
        @Generated
        private Integer id;
        @Generated
        private String slug;
        @Generated
        private MdmSport parent;
        @Generated
        private String domainKey;
        @Generated
        private boolean isEsports$set;
        @Generated
        private Boolean isEsports$value;
        @Generated
        private boolean isTeamSport$set;
        @Generated
        private Boolean isTeamSport$value;
        @Generated
        private boolean sortOrder$set;
        @Generated
        private Integer sortOrder$value;
        @Generated
        private OffsetDateTime createdAt;
        @Generated
        private OffsetDateTime updatedAt;

        @Generated
        MdmSportBuilder() {
        }

        @Generated
        public MdmSportBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmSportBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        @Generated
        public MdmSportBuilder parent(MdmSport parent) {
            this.parent = parent;
            return this;
        }

        @Generated
        public MdmSportBuilder domainKey(String domainKey) {
            this.domainKey = domainKey;
            return this;
        }

        @Generated
        public MdmSportBuilder isEsports(Boolean isEsports) {
            this.isEsports$value = isEsports;
            this.isEsports$set = true;
            return this;
        }

        @Generated
        public MdmSportBuilder isTeamSport(Boolean isTeamSport) {
            this.isTeamSport$value = isTeamSport;
            this.isTeamSport$set = true;
            return this;
        }

        @Generated
        public MdmSportBuilder sortOrder(Integer sortOrder) {
            this.sortOrder$value = sortOrder;
            this.sortOrder$set = true;
            return this;
        }

        @Generated
        public MdmSportBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmSportBuilder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public MdmSport build() {
            Boolean isEsports$value = this.isEsports$value;
            if (!this.isEsports$set) {
                isEsports$value = MdmSport.$default$isEsports();
            }
            Boolean isTeamSport$value = this.isTeamSport$value;
            if (!this.isTeamSport$set) {
                isTeamSport$value = MdmSport.$default$isTeamSport();
            }
            Integer sortOrder$value = this.sortOrder$value;
            if (!this.sortOrder$set) {
                sortOrder$value = MdmSport.$default$sortOrder();
            }
            return new MdmSport(this.id, this.slug, this.parent, this.domainKey, isEsports$value, isTeamSport$value, sortOrder$value, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "MdmSport.MdmSportBuilder(id=" + this.id + ", slug=" + this.slug + ", parent=" + String.valueOf(this.parent) + ", domainKey=" + this.domainKey + ", isEsports$value=" + this.isEsports$value + ", isTeamSport$value=" + this.isTeamSport$value + ", sortOrder$value=" + this.sortOrder$value + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

