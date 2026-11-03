package pro.datawiki.igaming.capture.fbref.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.capture.fbref.entity.FbrefRawReport;

import java.util.List;
import java.util.Optional;

@Repository
public interface FbrefRawReportRepository extends JpaRepository<FbrefRawReport, Long> {
    Optional<FbrefRawReport> findTopByMatchIdOrderByCreatedAtDesc(String matchId);
    List<FbrefRawReport> findByMatchId(String matchId);
    List<FbrefRawReport> findByStatus(String status);
}
