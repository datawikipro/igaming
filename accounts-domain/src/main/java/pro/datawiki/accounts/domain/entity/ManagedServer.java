package pro.datawiki.accounts.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A cloud VM provisioned and joined to the K3s cluster.
 * instanceName is the unique primary key (GCP/provider instance name).
 */
@Entity
@Table(name = "managed_servers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagedServer {

    /** Provider-level instance name (e.g. "spot-gcp-a1b2c3d4") */
    @Id
    private String instanceName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Account account;

    /** FK to cloud_credentials (not enforced at DB level for flexibility) */
    private Long cloudCredentialId;

    /** GCP, AWS, YANDEX, AZURE, DO, ORACLE, SELECTEL, VULTR */
    private String provider;

    private String region;
    private String zone;
    private String machineType;

    /** SPOT or STANDARD */
    private String provisioningModel;

    /** LOADER, MAIN */
    private String role;

    /** PROVISIONING → RUNNING → TERMINATING → TERMINATED */
    @Builder.Default
    private String status = "PROVISIONING";

    @Builder.Default
    private Boolean isFallback = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PROVISIONING";
        if (isFallback == null) isFallback = false;
    }
}
