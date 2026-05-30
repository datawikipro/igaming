package pro.datawiki.igaming.source.xbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class XbetResponse {

    @JsonProperty("Error")
    private String error;

    @JsonProperty("ErrorCode")
    private Integer errorCode;

    @JsonProperty("Guid")
    private String guid;

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("Success")
    private Boolean success;

    @JsonProperty("Value")
    private List<XbetGame> value;
}
