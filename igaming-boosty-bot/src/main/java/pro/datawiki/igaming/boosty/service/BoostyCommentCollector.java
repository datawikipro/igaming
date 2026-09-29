package pro.datawiki.igaming.boosty.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.boosty.boosty.BoostyComment;
import pro.datawiki.igaming.boosty.boosty.BoostyPost;
import pro.datawiki.igaming.boosty.model.BoostyDonorComment;
import pro.datawiki.igaming.boosty.repository.BoostyDonorCommentRepository;

import java.time.Instant;
import java.util.List;

/**
 * Service for collecting and persisting comments from Boosty donors.
 *
 * <p>Comments are filtered: only comments from paying subscribers (active donors)
 * are recorded. The filter is applied using {@link BoostyDonorService#isActiveDonor(Long)}.
 *
 * <p>New comments that haven't been forwarded to Telegram yet are queued
 * for forwarding by {@link BoostyTelegramNotifier}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BoostyCommentCollector {

    private final BoostyApiClient apiClient;
    private final BoostyDonorService donorService;
    private final BoostyDonorCommentRepository commentRepository;
    private final BoostyTelegramNotifier telegramNotifier;

    /**
     * Collect and store new comments from recent posts.
     *
     * <p>Workflow:
     * <ol>
     *   <li>Fetch the {@code maxPosts} most recent posts from Boosty</li>
     *   <li>For each post, fetch comments</li>
     *   <li>Filter comments: only accept those from active donors</li>
     *   <li>Persist new comments (skip duplicates by boosty_comment_id)</li>
     *   <li>Forward pending comments to Telegram donor chat</li>
     * </ol>
     *
     * @param maxPosts maximum number of recent posts to scan
     */
    @Transactional
    public void collectDonorComments(int maxPosts) {
        List<BoostyPost> posts = apiClient.fetchRecentPosts(maxPosts);
        log.debug("BoostyCommentCollector: scanning {} posts for donor comments", posts.size());

        int newCount = 0;
        for (BoostyPost post : posts) {
            if (post.getCommentCount() == null || post.getCommentCount() == 0) {
                continue; // skip posts with no comments
            }

            List<BoostyComment> comments = apiClient.fetchCommentsForPost(post.getId());
            for (BoostyComment comment : comments) {
                if (comment.getId() == null) continue;

                // Skip already recorded comments
                if (commentRepository.existsByBoostyCommentId(comment.getId())) {
                    continue;
                }

                // Extract author info
                Long authorUserId = extractAuthorUserId(comment);
                if (authorUserId == null) {
                    log.debug("BoostyCommentCollector: comment {} has no author userId, skipping", comment.getId());
                    continue;
                }

                // Filter: only from active donors
                if (!donorService.isActiveDonor(authorUserId)) {
                    log.debug("BoostyCommentCollector: comment {} from non-donor userId={}, skipping",
                            comment.getId(), authorUserId);
                    continue;
                }

                // Persist new donor comment
                Instant postedAt = comment.getCreatedAt() != null
                        ? Instant.ofEpochSecond(comment.getCreatedAt())
                        : Instant.now();

                BoostyDonorComment record = BoostyDonorComment.builder()
                        .boostyCommentId(comment.getId())
                        .boostyPostId(post.getId())
                        .boostyUserId(authorUserId)
                        .authorName(extractAuthorName(comment))
                        .content(comment.getContent())
                        .postedAt(postedAt)
                        .recordedAt(Instant.now())
                        .forwardedToTelegram(false)
                        .build();

                commentRepository.save(record);
                newCount++;
                log.info("BoostyCommentCollector: saved new donor comment id={} from userId={} on post={}",
                        comment.getId(), authorUserId, post.getId());
            }
        }

        log.debug("BoostyCommentCollector: collected {} new donor comments this cycle", newCount);

        // Forward pending comments to Telegram
        forwardPendingComments();
    }

    /**
     * Forward all unforwarded donor comments to the Telegram donor chat.
     */
    @Transactional
    public void forwardPendingComments() {
        List<BoostyDonorComment> pending = commentRepository.findAllByForwardedToTelegramFalse();
        if (pending.isEmpty()) return;

        log.debug("BoostyCommentCollector: forwarding {} pending comments to Telegram", pending.size());
        for (BoostyDonorComment comment : pending) {
            try {
                String message = String.format(
                        "💬 Комментарий Boosty-донора%n" +
                        "👤 %s%n" +
                        "📝 %s%n" +
                        "🔗 Post: %s",
                        comment.getAuthorName() != null ? comment.getAuthorName() : "Анонимный донор",
                        comment.getContent() != null ? comment.getContent() : "(пустой комментарий)",
                        comment.getBoostyPostId() != null ? "boosty.to/smartbetguru/..." : "N/A");

                telegramNotifier.sendToDonorChat(message);
                comment.setForwardedToTelegram(true);
                commentRepository.save(comment);
            } catch (Exception e) {
                log.warn("BoostyCommentCollector: failed to forward comment id={}: {}",
                        comment.getId(), e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────

    private Long extractAuthorUserId(BoostyComment comment) {
        if (comment.getAuthor() != null && comment.getAuthor().getUserId() != null) {
            return comment.getAuthor().getUserId();
        }
        return null;
    }

    private String extractAuthorName(BoostyComment comment) {
        if (comment.getAuthor() != null) {
            if (comment.getAuthor().getName() != null) return comment.getAuthor().getName();
            if (comment.getAuthor().getUserName() != null) return comment.getAuthor().getUserName();
        }
        return "Unknown";
    }
}
