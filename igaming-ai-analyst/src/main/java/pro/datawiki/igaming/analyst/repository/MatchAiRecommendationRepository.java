package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchAiRecommendationRepository extends JpaRepository<MatchAiRecommendation, Long>, JpaSpecificationExecutor<MatchAiRecommendation> {

    Optional<MatchAiRecommendation> findByMatchId(Long matchId);

    List<MatchAiRecommendation> findByResultStatus(String resultStatus);

    List<MatchAiRecommendation> findByResultStatusAndMatchStartTimeBefore(String resultStatus, LocalDateTime cutoffTime);

    List<MatchAiRecommendation> findTop50ByOrderByCreatedAtDesc();

    List<MatchAiRecommendation> findTop500ByOrderByIdDesc();

    @Query("SELECT r FROM MatchAiRecommendation r WHERE r.resultStatus != 'SKIPPED' ORDER BY r.id DESC")
    List<MatchAiRecommendation> findAllActiveAndSettled();

    List<MatchAiRecommendation> findTop150ByResultStatusOrderByIdDesc(String resultStatus);

    List<MatchAiRecommendation> findByResultStatusInOrderByIdDesc(List<String> statuses);

    @Query("SELECT COUNT(r) FROM MatchAiRecommendation r")
    long countTotalRecommendations();

    @Query("SELECT COUNT(r) FROM MatchAiRecommendation r WHERE r.resultStatus = 'WON'")
    long countWon();

    @Query("SELECT COUNT(r) FROM MatchAiRecommendation r WHERE r.resultStatus = 'LOST'")
    long countLost();

    @Query("SELECT COUNT(r) FROM MatchAiRecommendation r WHERE r.resultStatus = 'VOID'")
    long countVoid();

    @Query("SELECT COUNT(r) FROM MatchAiRecommendation r WHERE r.resultStatus = 'PENDING'")
    long countPending();

    @Query("SELECT COALESCE(SUM(r.actualPnl), 0) FROM MatchAiRecommendation r WHERE r.resultStatus IN ('WON', 'LOST', 'VOID')")
    java.math.BigDecimal sumActualPnl();
}
