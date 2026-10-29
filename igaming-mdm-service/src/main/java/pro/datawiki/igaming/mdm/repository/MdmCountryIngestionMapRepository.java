package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.mdm.domain.MdmCountryIngestionMap;

import java.util.List;
import java.util.Optional;

public interface MdmCountryIngestionMapRepository extends JpaRepository<MdmCountryIngestionMap, Long> {

    Optional<MdmCountryIngestionMap> findByExternalSystemIdAndRawKey(Integer externalSystemId, String rawKey);

    List<MdmCountryIngestionMap> findByExternalSystemId(Integer externalSystemId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO mdm_country_ingestion_map (external_system_id, raw_key, canonical_country_id, raw_name)
            VALUES (:sysId, :rawKey, :countryId, :rawName)
            ON CONFLICT (external_system_id, raw_key) DO NOTHING
            """, nativeQuery = true)
    int insertIgnoreConflict(@Param("sysId") Integer externalSystemId,
                             @Param("rawKey") String rawKey,
                             @Param("countryId") Integer countryId,
                             @Param("rawName") String rawName);
}
