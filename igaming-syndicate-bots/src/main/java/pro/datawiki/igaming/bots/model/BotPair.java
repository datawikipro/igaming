package pro.datawiki.igaming.bots.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotPair {
    private String pairName;
    private BotProfile bot1; // Russian operator
    private BotProfile bot2; // Cross-border international operator
}
