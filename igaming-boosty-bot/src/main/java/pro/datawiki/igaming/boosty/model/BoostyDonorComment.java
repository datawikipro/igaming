package pro.datawiki.igaming.boosty.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Persisted record of a comment left by a Boosty donor on a post.
 *
 * <p>Only comments from paying subscribers (donors with {@link BoostyDonorStatus#ACTIVE})
 * are collected and stored.
 *
 * <p>Table: {@code boosty_donor_comment}
 */
@Entity
@Table(name = "boosty_donor_comment",
       uniqueConstraints = @UniqueConstraint(name = "uq_boosty_comment_id", columnNames = "boosty_comment_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoostyDonorComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Boosty internal comment ID. */
    @Column(name = "boosty_comment_id", nullable = false)
    private Long boostyCommentId;

    /** Boosty post ID this comment belongs to. */
    @Column(name = "boosty_post_id", length = 128)
    private String boostyPostId;

    /** Boosty user ID of the commenter (must be an active donor). */
    @Column(name = "boosty_user_id", nullable = false)
    private Long boostyUserId;

    /** Display name of the commenter. */
    @Column(name = "author_name", length = 255)
    private String authorName;

    /** Raw comment text content (up to 4096 chars). */
    @Column(name = "content", length = 4096)
    private String content;

    /** When the comment was posted on Boosty. */
    @Column(name = "posted_at")
    private Instant postedAt;

    /** When we first recorded this comment in our DB. */
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    /** Whether this comment has been forwarded to Telegram. */
    @Column(name = "forwarded_to_telegram", nullable = false)
    private boolean forwardedToTelegram;
}
