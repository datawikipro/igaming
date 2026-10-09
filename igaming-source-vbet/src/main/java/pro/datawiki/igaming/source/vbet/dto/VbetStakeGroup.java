package pro.datawiki.igaming.source.vbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VbetStakeGroup {
    @JsonProperty("Id")
    private Long id;

    @JsonProperty("N")
    private String name;

    @JsonProperty("Stakes")
    private List<VbetStake> stakes = new ArrayList<>();
}
