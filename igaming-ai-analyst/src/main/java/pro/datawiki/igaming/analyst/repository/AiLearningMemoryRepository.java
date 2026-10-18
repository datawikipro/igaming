package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.AiLearningMemory;

import java.util.List;

@Repository
public interface AiLearningMemoryRepository extends JpaRepository<AiLearningMemory, Long> {

    @Query("SELECT m FROM AiLearningMemory m WHERE lower(m.teamName) IN (:teams) OR lower(m.leagueName) = lower(:league) ORDER BY m.createdAt DESC")
    List<AiLearningMemory> findRelevantLessons(@Param("teams") List<String> teams, @Param("league") String league);

    List<AiLearningMemory> findTop20ByOrderByCreatedAtDesc();
}
