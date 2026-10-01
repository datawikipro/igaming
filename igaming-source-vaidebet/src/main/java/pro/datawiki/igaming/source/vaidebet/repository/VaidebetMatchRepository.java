package pro.datawiki.igaming.source.vaidebet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.source.vaidebet.entity.VaidebetMatchEntity;

import java.util.Optional;

@Repository
public interface VaidebetMatchRepository extends JpaRepository<VaidebetMatchEntity, Long> {
    Optional<VaidebetMatchEntity> findByExternalId(String externalId);
}
