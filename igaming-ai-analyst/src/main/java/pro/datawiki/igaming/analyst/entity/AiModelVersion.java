package pro.datawiki.igaming.analyst.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_model_version")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiModelVersion {

    @Id
    @Column(name = "version_code", length = 32, nullable = false)
    private String versionCode; // e.g. "v0.3.2.1.1"

    @Column(name = "display_name", length = 128, nullable = false)
    private String displayName;

    @Column(name = "ideology_version", length = 32, nullable = false)
    @Builder.Default
    private String ideologyVersion = "3"; // 3 = Scenarios & Strategy Cohorts

    @Column(name = "vector_version", length = 32, nullable = false)
    @Builder.Default
    private String vectorVersion = "2"; // 2 = Bayesian Shrinkage + SofaScore DDS Facts

    @Column(name = "loader_version", length = 32, nullable = false)
    @Builder.Default
    private String loaderVersion = "1"; // 1 = ClickHouse DDS Match Fact Loader

    @Column(name = "iteration_number", nullable = false)
    @Builder.Default
    private Integer iterationNumber = 1;

    @Column(name = "release_date", nullable = false)
    private LocalDateTime releaseDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = false;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "changelog", columnDefinition = "TEXT")
    private String changelog;
}
