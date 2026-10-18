package pro.datawiki.igaming.analyst.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShadowPickDto {
    private String type;           // e.g. "DRAW_DEFENSE", "CUSHION_TOTAL", "CONTRARIAN_HEDGE"
    private String market;         // e.g. "DOUBLE_CHANCE_1X", "TOTAL_UNDER_3.5", "AH_HOME_0"
    private BigDecimal odd;        // e.g. 1.45
    private String hypothesis;     // Why this contingency pick survives if our main fear occurs
    private String resultStatus;   // PENDING, WON, LOST, VOID
}
