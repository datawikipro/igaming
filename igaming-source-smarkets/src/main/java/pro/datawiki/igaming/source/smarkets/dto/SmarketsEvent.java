package pro.datawiki.igaming.source.smarkets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmarketsEvent {
    private String id;
    private String name;
    private String type;

    @JsonProperty("start_datetime")
    private String startDatetime;

    private String state;

    @JsonProperty("parent_id")
    private String parentId;

    private String slug;

    @JsonProperty("short_name")
    private String shortName;

    private Boolean bettable;
}
