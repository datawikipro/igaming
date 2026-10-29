package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.mdm.domain.MdmOrganization;

import java.util.Optional;

@Repository
public interface MdmOrganizationRepository extends JpaRepository<MdmOrganization, Long> {

    Optional<MdmOrganization> findBySlug(String slug);

    Optional<MdmOrganization> findByCanonicalNameIgnoreCase(String canonicalName);
}
