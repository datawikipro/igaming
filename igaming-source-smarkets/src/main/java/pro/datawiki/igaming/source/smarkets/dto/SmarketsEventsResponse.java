package pro.datawiki.igaming.source.smarkets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmarketsEventsResponse {
    private List<SmarketsEvent> events = new ArrayList<>();
}
