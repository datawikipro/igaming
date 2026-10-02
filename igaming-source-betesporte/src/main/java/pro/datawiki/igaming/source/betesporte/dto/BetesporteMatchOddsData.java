package pro.datawiki.igaming.source.betesporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetesporteMatchOddsData {
    private Long matchId;
    private List<BetesporteStakeGroupData> groups;
}
