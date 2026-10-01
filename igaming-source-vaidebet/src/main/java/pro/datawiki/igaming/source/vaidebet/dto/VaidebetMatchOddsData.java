package pro.datawiki.igaming.source.vaidebet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaidebetMatchOddsData {
    private Long matchId;
    private List<VaidebetStakeGroupData> groups;
}
