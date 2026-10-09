package pro.datawiki.igaming.source.stake.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StakeGraphQLRequest {
    private String query;
    private Map<String, Object> variables;

    public StakeGraphQLRequest(String query) {
        this.query = query;
    }
}
