package pro.datawiki.igaming.source.apuestatotal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.source.apuestatotal.entity.ApuestatotalMatchEntity;

import java.util.Optional;

@Repository
public interface ApuestatotalMatchRepository extends JpaRepository<ApuestatotalMatchEntity, Long> {
    Optional<ApuestatotalMatchEntity> findByExternalId(String externalId);
}
