package pro.datawiki.accounts.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Point-in-time snapshot of a cloud account's billing and compute state.
 * Written by cloud-poller every N minutes.
 */
@Entity
@Table(name = "cloud_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to cloud_credentials */
    @Column(nullable = false)
    private Long cloudCredentialId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /** Current billing balance (may be null if Billing API not accessible) */
    @Column(precision = 18, scale = 4)
    private BigDecimal balance;

    /** Currency code, e.g. "USD", "RUB" */
    private String currency;

    /** Total VMs visible in provider */
    private Integer serverCount;

    /** VMs in RUNNING state */
    private Integer runningCount;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }
}
