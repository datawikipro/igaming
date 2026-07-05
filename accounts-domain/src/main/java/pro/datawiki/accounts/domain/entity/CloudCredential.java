package pro.datawiki.accounts.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Cloud provider credentials for a given Account.
 * One Account may have credentials for multiple cloud providers.
 *
 * Supported providers: GCP, AWS, YANDEX, AZURE, DO, ORACLE, SELECTEL, VULTR
 */
@Entity
@Table(name = "cloud_credentials")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Account account;

    /** GCP, AWS, YANDEX, AZURE, DO, ORACLE, SELECTEL, VULTR */
    @Column(nullable = false)
    private String provider;

    /** GCP: project ID. Yandex: folder ID. AWS: account ID. */
    private String projectId;

    private String defaultRegion;
    private String defaultZone;

    /**
     * GCP: service account JSON.
     * AWS: JSON {"accessKeyId":"...","secretAccessKey":"..."}.
     * YANDEX: service account JSON or IAM token.
     * Others: provider-specific JSON payload.
     */
    @Column(columnDefinition = "TEXT")
    private String credentialsJson;

    // K3s cluster connection (node join)
    private String k3sServerUrl;

    @Column(columnDefinition = "TEXT")
    private String k3sToken;

    // Tailscale auth key for mesh VPN
    private String tailscaleAuthKey;

    @Builder.Default
    private Boolean isActive = true;

    /** Lower number = higher priority when provisioning */
    @Builder.Default
    private Integer priority = 100;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (priority == null) priority = 100;
    }
}
