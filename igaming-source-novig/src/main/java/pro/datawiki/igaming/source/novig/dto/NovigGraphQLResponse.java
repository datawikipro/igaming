package pro.datawiki.igaming.source.novig.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NovigGraphQLResponse {
    private DataContainer data;
    private List<Map<String, Object>> errors;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataContainer {
        private List<NovigEventDto> event;
    }
}
