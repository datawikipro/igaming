package pro.datawiki.igaming.playerfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;

import java.util.List;

/**
 * Request payload for batch resolving player faces by names.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerBatchRequest {

    private List<String> playerNames;
    private SportType sport;
    private TourType tour;
}
