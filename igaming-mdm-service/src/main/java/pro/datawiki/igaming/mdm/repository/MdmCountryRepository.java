package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmCountry;

import java.util.Optional;

public interface MdmCountryRepository extends JpaRepository<MdmCountry, Integer> {

    Optional<MdmCountry> findFirstByCanonicalNameIgnoreCase(String canonicalName);

    Optional<MdmCountry> findFirstByIsoCodeIgnoreCase(String isoCode);

    Optional<MdmCountry> findFirstBySofascoreId(Integer sofascoreId);
}
