package pro.datawiki.igaming.source.digitain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.source.digitain.entity.DigitainMatchEntity;

import java.util.Optional;

@Repository
public interface DigitainMatchRepository extends JpaRepository<DigitainMatchEntity, Long> {
    Optional<DigitainMatchEntity> findByExternalId(String externalId);
}
