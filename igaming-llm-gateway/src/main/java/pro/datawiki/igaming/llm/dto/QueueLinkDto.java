package pro.datawiki.igaming.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueLinkDto {
    private Long id;
    private String sourceProvider;
    private String sourceModel;
    private String targetProvider;
    private String targetModel;
    private boolean active;
    private LocalDateTime createdAt;
}
