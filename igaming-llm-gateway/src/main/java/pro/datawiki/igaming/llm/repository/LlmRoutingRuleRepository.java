package pro.datawiki.igaming.llm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.domain.LlmRoutingRule;
import java.util.List;

@Repository
public interface LlmRoutingRuleRepository extends JpaRepository<LlmRoutingRule, Long> {
    List<LlmRoutingRule> findByActiveTrue();
}
