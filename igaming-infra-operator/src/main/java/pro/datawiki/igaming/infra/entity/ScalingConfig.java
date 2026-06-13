package pro.datawiki.igaming.infra.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scaling_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScalingConfig {
    @Id
    private String configKey;
    private Integer configValue;
}
