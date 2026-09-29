package pro.datawiki.igaming.boosty.boosty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Boosty API paginated response wrapper.
 *
 * <p>Boosty wraps paginated list responses in a structure with a {@code data} field
 * and optional {@code extra} pagination cursor.
 *
 * <p>Example:
 * <pre>
 * {
 *   "data": [ ... ],
 *   "extra": { "offset": 10, "total": 42 }
 * }
 * </pre>
 *
 * @param <T> the type of items in the {@code data} list
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoostyPagedResponse<T> {

    @JsonProperty("data")
    private List<T> data;

    @JsonProperty("extra")
    private BoostyPaginationExtra extra;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoostyPaginationExtra {
        @JsonProperty("offset")
        private Integer offset;
        @JsonProperty("total")
        private Integer total;
    }
}
