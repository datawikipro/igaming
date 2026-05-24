package pro.datawiki.igaming.llm.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "llm_routing_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LlmRoutingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logical_type", nullable = false, length = 64)
    private String logicalType;

    @Column(name = "google_required")
    private Boolean googleRequired;

    @Column(name = "urgency", length = 32)
    private String urgency;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_model_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LlmModel targetModel;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
