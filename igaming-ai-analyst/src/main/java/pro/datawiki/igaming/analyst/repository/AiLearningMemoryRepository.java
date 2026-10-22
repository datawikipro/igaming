package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.AiLearningMemory;

import java.util.List;

@Repository
public interface AiLearningMemoryRepository extends JpaRepository<AiLearningMemory, Long> {

    @Query("SELECT m FROM AiLearningMemory m WHERE (m.sportScope IS NULL OR m.sportScope IN (:scopes)) AND (m.teamName IN ('GLOBAL_CALIBRATION', 'GLOBAL_TOURNAMENT_CALIBRATION') OR lower(m.teamName) IN (:teams) OR lower(m.leagueName) = lower(:league)) ORDER BY m.createdAt DESC")
    List<AiLearningMemory> findRelevantLessons(@Param("teams") List<String> teams, @Param("league") String league, @Param("scopes") List<String> scopes);

    @Query("SELECT m FROM AiLearningMemory m WHERE m.sportScope IS NULL OR m.sportScope IN (:scopes) ORDER BY m.createdAt DESC")
    List<AiLearningMemory> findByScopes(@Param("scopes") List<String> scopes);

    List<AiLearningMemory> findTop20ByOrderByCreatedAtDesc();
}
