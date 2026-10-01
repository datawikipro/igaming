package pro.datawiki.igaming.source.esportesdasorte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.source.esportesdasorte.entity.EsportesdasorteMatchEntity;

import java.util.Optional;

@Repository
public interface EsportesdasorteMatchRepository extends JpaRepository<EsportesdasorteMatchEntity, Long> {
    Optional<EsportesdasorteMatchEntity> findByExternalId(String externalId);
}
