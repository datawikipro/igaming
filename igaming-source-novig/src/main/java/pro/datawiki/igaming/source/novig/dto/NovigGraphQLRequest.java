package pro.datawiki.igaming.source.novig.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NovigGraphQLRequest {
    private String query;
    private Map<String, Object> variables;

    public NovigGraphQLRequest(String query) {
        this.query = query;
    }
}
