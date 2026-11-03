package pro.datawiki.igaming.capture.fbref.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.capture.fbref.entity.UnderstatRawShot;

import java.util.List;

@Repository
public interface UnderstatRawShotRepository extends JpaRepository<UnderstatRawShot, Long> {
    List<UnderstatRawShot> findByMatchId(String matchId);
    List<UnderstatRawShot> findByMatchIdOrderByMinuteAsc(String matchId);
    List<UnderstatRawShot> findByStatus(String status);
}
