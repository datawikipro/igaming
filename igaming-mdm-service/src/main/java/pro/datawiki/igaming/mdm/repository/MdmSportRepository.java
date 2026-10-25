package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmSport;

import java.util.Optional;

public interface MdmSportRepository extends JpaRepository<MdmSport, Integer> {
    Optional<MdmSport> findBySlug(String slug);
    Optional<MdmSport> findByDomainKey(String domainKey);
}
