package pro.datawiki.igaming.boosty.boosty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Boosty API comment record from GET /v1/blog/{blog_name}/post/{post_id}/comment
 *
 * <p>Comments are fetched from individual posts belonging to the blog.
 * Only comments from paying subscribers are stored per business requirements.
 *
 * <p>Example:
 * <pre>
 * {
 *   "id": 98765,
 *   "post_id": "abc-def-123",
 *   "author": { "id": 12345, "name": "Иван", "username": "ivanpetrov" },
 *   "content": [{ "type": "plain", "content": "Отличный сигнал!" }],
 *   "created_at": 1700000000
 * }
 * </pre>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoostyComment {

    /** Boosty internal comment ID. */
    @JsonProperty("id")
    private Long id;

    /** ID of the post this comment belongs to. */
    @JsonProperty("post_id")
    private String postId;

    /** Comment author info. */
    @JsonProperty("intl_data")
    private BoostyCommentAuthor author;

    /** Raw plain-text content (extracted from content array). */
    @JsonProperty("content")
    private String content;

    /** Unix timestamp when the comment was created. */
    @JsonProperty("created_at")
    private Long createdAt;

    /** Comment author details. */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoostyCommentAuthor {
        @JsonProperty("userId")
        private Long userId;
        @JsonProperty("userName")
        private String userName;
        @JsonProperty("name")
        private String name;
    }
}
