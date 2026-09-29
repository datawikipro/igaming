package pro.datawiki.igaming.boosty.boosty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Boosty API post record (abbreviated) from GET /v1/blog/{blog_name}/post
 *
 * <p>Used only to obtain post IDs for comment collection.
 * Full post content is not stored; only IDs are iterated.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoostyPost {

    /** Boosty internal post ID (string UUID). */
    @JsonProperty("id")
    private String id;

    /** Post title. */
    @JsonProperty("title")
    private String title;

    /** Unix timestamp of post publication. */
    @JsonProperty("publishTime")
    private Long publishTime;

    /** Number of comments on this post. */
    @JsonProperty("commentCount")
    private Integer commentCount;
}
