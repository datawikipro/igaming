/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Table
 *  lombok.Generated
 *  org.hibernate.annotations.CreationTimestamp
 */
package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name="mdm_external_system")
public class MdmExternalSystem {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable=false, unique=true, length=64)
    private String code;
    @Column(nullable=false, length=128)
    private String name;
    @Column(name="is_active", nullable=false)
    private Boolean isActive;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Generated
    private static Boolean $default$isActive() {
        return true;
    }

    @Generated
    public static MdmExternalSystemBuilder builder() {
        return new MdmExternalSystemBuilder();
    }

    @Generated
    public Integer getId() {
        return this.id;
    }

    @Generated
    public String getCode() {
        return this.code;
    }

    @Generated
    public String getName() {
        return this.name;
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
    public void setId(Integer id) {
        this.id = id;
    }

    @Generated
    public void setCode(String code) {
        this.code = code;
    }

    @Generated
    public void setName(String name) {
        this.name = name;
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
    public MdmExternalSystem() {
        this.isActive = MdmExternalSystem.$default$isActive();
    }

    @Generated
    public MdmExternalSystem(Integer id, String code, String name, Boolean isActive, OffsetDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    @Generated
    public static class MdmExternalSystemBuilder {
        @Generated
        private Integer id;
        @Generated
        private String code;
        @Generated
        private String name;
        @Generated
        private boolean isActive$set;
        @Generated
        private Boolean isActive$value;
        @Generated
        private OffsetDateTime createdAt;

        @Generated
        MdmExternalSystemBuilder() {
        }

        @Generated
        public MdmExternalSystemBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        @Generated
        public MdmExternalSystemBuilder code(String code) {
            this.code = code;
            return this;
        }

        @Generated
        public MdmExternalSystemBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Generated
        public MdmExternalSystemBuilder isActive(Boolean isActive) {
            this.isActive$value = isActive;
            this.isActive$set = true;
            return this;
        }

        @Generated
        public MdmExternalSystemBuilder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public MdmExternalSystem build() {
            Boolean isActive$value = this.isActive$value;
            if (!this.isActive$set) {
                isActive$value = MdmExternalSystem.$default$isActive();
            }
            return new MdmExternalSystem(this.id, this.code, this.name, isActive$value, this.createdAt);
        }

        @Generated
        public String toString() {
            return "MdmExternalSystem.MdmExternalSystemBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", isActive$value=" + this.isActive$value + ", createdAt=" + String.valueOf(this.createdAt) + ")";
        }
    }
}

