package pro.datawiki.igaming.source.betfair.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a single price point on an exchange ladder with available liquidity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceSizeLadder implements Serializable {
    private Double price;
    private Double size;
}
