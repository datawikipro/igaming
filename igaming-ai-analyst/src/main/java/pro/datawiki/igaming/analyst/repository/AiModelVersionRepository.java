package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.AiModelVersion;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiModelVersionRepository extends JpaRepository<AiModelVersion, String> {

    List<AiModelVersion> findAllByOrderByReleaseDateDesc();

    Optional<AiModelVersion> findByIsActiveTrue();
}
