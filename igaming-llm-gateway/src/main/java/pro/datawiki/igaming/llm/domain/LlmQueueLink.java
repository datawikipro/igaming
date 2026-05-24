package pro.datawiki.igaming.llm.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "llm_queue_links")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmQueueLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String sourceProvider;

    @Column(nullable = false, length = 128)
    private String sourceModel;

    @Column(nullable = false, length = 64)
    private String targetProvider;

    @Column(nullable = false, length = 128)
    private String targetModel;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
