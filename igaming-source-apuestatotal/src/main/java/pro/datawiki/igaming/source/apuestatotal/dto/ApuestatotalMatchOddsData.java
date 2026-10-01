package pro.datawiki.igaming.source.apuestatotal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApuestatotalMatchOddsData {
    private Long matchId;
    private List<ApuestatotalStakeGroupData> groups;
}
