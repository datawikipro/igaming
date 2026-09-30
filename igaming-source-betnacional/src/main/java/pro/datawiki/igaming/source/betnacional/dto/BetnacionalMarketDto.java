package pro.datawiki.igaming.source.betnacional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BetnacionalMarketDto {

    private String id;
    private String name;
    private String title;
    private String marketType;
    private String category;
    private String group;
    private String period;
    private String status;
    @Builder.Default
    private List<BetnacionalOutcomeDto> outcomes = new ArrayList<>();

    public String getEffectiveName() {
        if (name != null && !name.isBlank()) return name;
        if (title != null && !title.isBlank()) return title;
        if (marketType != null && !marketType.isBlank()) return marketType;
        return "";
    }
}
