package pro.datawiki.igaming.source.superbet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuperbetInPlayMetadata {
    private String status;
}
