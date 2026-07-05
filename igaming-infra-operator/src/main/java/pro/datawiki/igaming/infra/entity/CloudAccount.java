package pro.datawiki.igaming.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cloud_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String provider; // GCP, AWS, YANDEX
    private String projectId; // Project ID for GCP, Folder ID for Yandex
    private String defaultRegion;
    private String defaultZone;

    @Column(columnDefinition = "TEXT")
    private String credentialsJson; // Service account JSON or access token

    private Boolean isActive;
    private Integer priority; // Lower number means higher priority

    // K3s cluster connection details (overrides global env if set)
    private String k3sServerUrl;  // e.g. https://master-vm-1:6443
    @Column(columnDefinition = "TEXT")
    private String k3sToken;      // K3s node join token

    // Per-account Tailscale auth key (overrides global env if set)
    private String tailscaleAuthKey;

    private LocalDateTime createdAt;
}
