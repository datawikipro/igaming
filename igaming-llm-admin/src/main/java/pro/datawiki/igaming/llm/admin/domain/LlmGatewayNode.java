package pro.datawiki.igaming.llm.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "llm_gateway_nodes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LlmGatewayNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String endpointUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "model_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LlmModel model;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private String status = "IDLE"; // "IDLE", "STARTED", "HEALTHY", "EXHAUSTED", "DOWN"

    private LocalDateTime suspendedUntil;

    @Builder.Default
    private long successCount = 0;

    @Builder.Default
    private long failureCount = 0;

    @Builder.Default
    private long totalTokensUsed = 0;

    private LocalDateTime lastRequestTime;

    private String leasedByPod;

    private LocalDateTime leasedAt;

    public boolean isAvailable() {
        if (!active) return false;
        if (suspendedUntil != null && suspendedUntil.isAfter(LocalDateTime.now())) {
            return false;
        }
        return !"DOWN".equals(status);
    }
}
