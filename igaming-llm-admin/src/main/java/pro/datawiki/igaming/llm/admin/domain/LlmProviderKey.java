package pro.datawiki.igaming.llm.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "llm_provider_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LlmProviderKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id", nullable = false)
    @JsonIgnoreProperties({"keys", "hibernateLazyInitializer", "handler"})
    private LlmProvider provider;

    /** Человекочитаемый ярлык: "Google Account #1 Pro", "DeepSeek Key A" */
    @Column(nullable = false, length = 128)
    private String label;

    /** Ключ API или JSON credentials */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String apiKey;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "key", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("key")
    private java.util.List<LlmKeyModelSuspension> modelSuspensions;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

