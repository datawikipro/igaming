package pro.datawiki.igaming.source.favbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FavbetResponse<T> {
    @JsonProperty("result")
    private List<T> result = new ArrayList<>();

    @JsonProperty("response_code")
    private Integer responseCode;

    @JsonProperty("error")
    private String error;
}
