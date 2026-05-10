package pro.datawiki.igaming.llm.admin.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "llm_gateway_nodes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmGatewayNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String endpointUrl;

    @Column(nullable = false)
    private String providerType; // e.g. "gemini", "deepseek", "agent-studio"

    @Column(nullable = false)
    private String modelName; // e.g. "gemini-3-flash-preview"

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private String status = "HEALTHY"; // "HEALTHY", "EXHAUSTED", "DOWN"

    private LocalDateTime suspendedUntil;

    @Builder.Default
    private long successCount = 0;

    @Builder.Default
    private long failureCount = 0;

    @Builder.Default
    private long totalTokensUsed = 0;

    private LocalDateTime lastRequestTime;

    private String apiKey;

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
