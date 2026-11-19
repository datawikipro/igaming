package pro.datawiki.igaming.analyst.rating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SportMmrRatingId implements Serializable {
    private Long playerId;
    private String sportSlug;
    private String surface;
}
