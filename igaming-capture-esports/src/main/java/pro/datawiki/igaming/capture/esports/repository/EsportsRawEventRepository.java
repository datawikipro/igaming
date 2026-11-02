package pro.datawiki.igaming.capture.esports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.capture.esports.domain.EsportsRawEventEntity;

import java.util.List;

@Repository
public interface EsportsRawEventRepository extends JpaRepository<EsportsRawEventEntity, String> {

    List<EsportsRawEventEntity> findByMdmStatus(String mdmStatus);

    List<EsportsRawEventEntity> findByMdmStatusAndGame(String mdmStatus, String game);

    List<EsportsRawEventEntity> findByGame(String game);

    List<EsportsRawEventEntity> findByGameAndStatus(String game, String status);

    @Query("SELECT e FROM EsportsRawEventEntity e WHERE e.mdmStatus = 'UNMAPPED' ORDER BY e.createdAt DESC")
    List<EsportsRawEventEntity> findUnmapped();

    @Query("SELECT COUNT(e) FROM EsportsRawEventEntity e WHERE e.mdmStatus = :status")
    long countByMdmStatus(@Param("status") String status);

    @Query("SELECT e.game, e.mdmStatus, COUNT(e) FROM EsportsRawEventEntity e GROUP BY e.game, e.mdmStatus")
    List<Object[]> countSummaryByGameAndMdmStatus();
}
