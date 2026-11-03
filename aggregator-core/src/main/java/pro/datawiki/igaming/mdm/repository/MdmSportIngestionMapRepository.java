/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 */
package pro.datawiki.igaming.mdm.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.datawiki.igaming.mdm.domain.MdmSportIngestionMap;

public interface MdmSportIngestionMapRepository
extends JpaRepository<MdmSportIngestionMap, Long> {
    public Optional<MdmSportIngestionMap> findByExternalSystemIdAndRawKey(Integer var1, String var2);

    @Query(value="SELECT m FROM MdmSportIngestionMap m JOIN FETCH m.canonicalSport s WHERE m.externalSystem.id = :externalSystemId")
    public List<MdmSportIngestionMap> findByExternalSystemIdWithFetch(@Param(value="externalSystemId") Integer var1);

    public List<MdmSportIngestionMap> findByExternalSystemId(Integer var1);
}

