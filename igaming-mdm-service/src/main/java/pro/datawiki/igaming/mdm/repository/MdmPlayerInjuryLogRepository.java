package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.mdm.domain.MdmPlayerInjuryLog;

import java.util.List;

@Repository
public interface MdmPlayerInjuryLogRepository extends JpaRepository<MdmPlayerInjuryLog, Long> {

    List<MdmPlayerInjuryLog> findByPlayerIdOrderByStartedAtDesc(Long playerId);

    List<MdmPlayerInjuryLog> findByPlayerIdAndActualReturnedAtIsNullOrderByStartedAtDesc(Long playerId);
}
