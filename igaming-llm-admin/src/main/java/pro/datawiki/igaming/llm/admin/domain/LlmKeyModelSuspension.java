package pro.datawiki.igaming.llm.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "llm_key_model_suspensions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LlmKeyModelSuspension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "key_id", nullable = false)
    @JsonIgnoreProperties({"provider", "hibernateLazyInitializer", "handler"})
    private LlmProviderKey key;

    @Column(name = "model_name", nullable = false, length = 128)
    private String modelName;

    @Column(name = "suspended_until", nullable = false)
    private LocalDateTime suspendedUntil;
}
