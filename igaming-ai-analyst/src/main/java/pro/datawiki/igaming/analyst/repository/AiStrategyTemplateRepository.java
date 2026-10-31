package pro.datawiki.igaming.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.analyst.entity.AiStrategyTemplate;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiStrategyTemplateRepository extends JpaRepository<AiStrategyTemplate, Long> {

    Optional<AiStrategyTemplate> findBySlug(String slug);

    Optional<AiStrategyTemplate> findByIsSystemDefaultTrue();

    List<AiStrategyTemplate> findAllByOrderBySortOrderAsc();

    List<AiStrategyTemplate> findByUserIdOrderBySortOrderAsc(Long userId);
}
