package pro.datawiki.igaming.source.betrivers.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiEventDto {
    private Long id;
    private String name;
    private String englishName;
    private String homeName;
    private String awayName;
    private String start;
    private String group;
    private Long groupId;
    private String sport;
    private String state;
    private Integer nonLiveBoCount;
    private List<KambiPathItem> path;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KambiPathItem {
        private Long id;
        private String name;
        private String englishName;
        private String termKey;
    }
}
