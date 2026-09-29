package pro.datawiki.igaming.boosty.boosty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Boosty API subscriber record from GET /v1/blog/{blog_name}/subscribers
 *
 * <p>Boosty does not provide an official public API, so this DTO is based on
 * reverse-engineering of the internal API used by the Boosty web app.
 * Reference community libs: akovardin/boosty (Go), PyBoostyApi (Python).
 *
 * <p>Example response excerpt:
 * <pre>
 * {
 *   "id": 12345,
 *   "name": "Иван",
 *   "username": "ivanpetrov",
 *   "price": 299,
 *   "level": { "title": "Premium" }
 * }
 * </pre>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoostySubscriber {

    /** Boosty internal user numeric ID. */
    @JsonProperty("id")
    private Long id;

    /** Display name of the subscriber. */
    @JsonProperty("name")
    private String name;

    /** Boosty username/slug. */
    @JsonProperty("username")
    private String username;

    /** Monthly subscription price in rubles. */
    @JsonProperty("price")
    private Integer price;

    /** Subscription level / tier. */
    @JsonProperty("level")
    private BoostySubscriptionLevel level;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoostySubscriptionLevel {
        @JsonProperty("title")
        private String title;
        @JsonProperty("id")
        private Long id;
    }
}
