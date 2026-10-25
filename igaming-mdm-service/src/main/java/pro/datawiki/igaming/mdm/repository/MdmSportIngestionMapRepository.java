package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmSportIngestionMap;

import java.util.List;
import java.util.Optional;

public interface MdmSportIngestionMapRepository extends JpaRepository<MdmSportIngestionMap, Long> {
    Optional<MdmSportIngestionMap> findByExternalSystemIdAndRawKey(Integer externalSystemId, String rawKey);

    @Query("SELECT m FROM MdmSportIngestionMap m JOIN FETCH m.canonicalSport s WHERE m.externalSystem.id = :externalSystemId")
    List<MdmSportIngestionMap> findByExternalSystemIdWithFetch(@Param("externalSystemId") Integer externalSystemId);

    List<MdmSportIngestionMap> findByExternalSystemId(Integer externalSystemId);
}
