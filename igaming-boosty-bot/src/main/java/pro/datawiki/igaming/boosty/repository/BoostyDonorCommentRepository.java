package pro.datawiki.igaming.boosty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.boosty.model.BoostyDonorComment;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link BoostyDonorComment}.
 */
@Repository
public interface BoostyDonorCommentRepository extends JpaRepository<BoostyDonorComment, Long> {

    Optional<BoostyDonorComment> findByBoostyCommentId(Long boostyCommentId);

    List<BoostyDonorComment> findAllByBoostyUserIdOrderByPostedAtDesc(Long boostyUserId);

    List<BoostyDonorComment> findAllByForwardedToTelegramFalse();

    boolean existsByBoostyCommentId(Long boostyCommentId);
}
