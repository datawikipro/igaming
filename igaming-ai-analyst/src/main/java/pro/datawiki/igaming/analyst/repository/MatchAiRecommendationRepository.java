package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.MatchAiRecommendation;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchAiRecommendationRepository extends JpaRepository<MatchAiRecommendation, Long>, JpaSpecificationExecutor<MatchAiRecommendation> {

    Optional<MatchAiRecommendation> findFirstByMatchIdOrderByModelVersionDesc(Long matchId);

    Optional<MatchAiRecommendation> findByMatchIdAndModelVersion(Long matchId, String modelVersion);

    List<MatchAiRecommendation> findAllByMatchId(Long matchId);

    List<MatchAiRecommendation> findByMatchIdInOrderByIdDesc(Collection<Long> matchIds);

    default Optional<MatchAiRecommendation> findByMatchId(Long matchId) {
        return findFirstByMatchIdOrderByModelVersionDesc(matchId);
    }

    List<MatchAiRecommendation> findByEvaluationStage(String evaluationStage);

    List<MatchAiRecommendation> findByMatchIdAndEvaluationStage(Long matchId, String evaluationStage);

    List<MatchAiRecommendation> findByMatchIdAndEvaluationStageNot(Long matchId, String evaluationStage);

    List<MatchAiRecommendation> findByEvaluationStageNotIn(Collection<String> stages);

    List<MatchAiRecommendation> findByEvaluationStageNotInOrderByIdDesc(Collection<String> stages);

    List<MatchAiRecommendation> findByEvaluationStageAndUpdatedAtAfter(String evaluationStage, LocalDateTime after);

    long countByEvaluationStageNotIn(Collection<String> stages);

    @Query("SELECT r FROM MatchAiRecommendation r WHERE r.evaluationStage NOT IN ('SETTLED', 'CANCELLED', 'SKIPPED', 'AWAITING_SETTLEMENT') AND r.matchStartTime <= :cutoff")
    List<MatchAiRecommendation> findPendingNeedingLiveTransition(LocalDateTime cutoff);

    List<MatchAiRecommendation> findTop50ByOrderByCreatedAtDesc();

    List<MatchAiRecommendation> findTop500ByOrderByIdDesc();

    List<MatchAiRecommendation> findTop200BySportNameIgnoreCaseAndEvaluationStageNotInOrderByIdDesc(String sportName, Collection<String> stages);

    List<MatchAiRecommendation> findTop200ByEvaluationStageNotInOrderByIdDesc(Collection<String> stages);

    @Query("SELECT r FROM MatchAiRecommendation r WHERE r.evaluationStage != 'SKIPPED' ORDER BY r.id DESC")
    List<MatchAiRecommendation> findAllActiveAndSettled();

    List<MatchAiRecommendation> findTop150ByEvaluationStageOrderByIdDesc(String evaluationStage);

    List<MatchAiRecommendation> findByEvaluationStageOrderByIdDesc(String evaluationStage);

    @Query("SELECT COUNT(r) FROM MatchAiRecommendation r")
    long countTotalRecommendations();

    @Query(value = """
        SELECT DISTINCT r.match_id 
        FROM match_ai_recommendation r
        WHERE r.match_id IS NOT NULL
          AND NOT EXISTS (
              SELECT 1 FROM match_ai_recommendation r2 
              WHERE r2.match_id = r.match_id AND r2.model_version = :currentVersion
          )
        ORDER BY r.match_id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Long> findMatchIdsNeedingVersionBackfill(@org.springframework.data.repository.query.Param("currentVersion") String currentVersion, @org.springframework.data.repository.query.Param("limit") int limit);
}
