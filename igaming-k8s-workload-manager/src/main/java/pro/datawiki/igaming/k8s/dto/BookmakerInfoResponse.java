package pro.datawiki.igaming.k8s.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmakerInfoResponse {
    private String name;
    private String family;
}
