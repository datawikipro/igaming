package pro.datawiki.igaming.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.mdm.domain.MdmExternalSystem;

import java.util.Optional;

public interface MdmExternalSystemRepository extends JpaRepository<MdmExternalSystem, Integer> {
    Optional<MdmExternalSystem> findByCode(String code);
}
