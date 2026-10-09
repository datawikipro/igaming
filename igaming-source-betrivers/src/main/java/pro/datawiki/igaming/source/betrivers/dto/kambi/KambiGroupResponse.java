package pro.datawiki.igaming.source.betrivers.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiGroupResponse {
    private KambiGroupNode group;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KambiGroupNode {
        private Long id;
        private String name;
        private String englishName;
        private String sport;
        private Integer eventCount;
        private String termKey;
        private List<KambiGroupNode> groups;
    }
}
