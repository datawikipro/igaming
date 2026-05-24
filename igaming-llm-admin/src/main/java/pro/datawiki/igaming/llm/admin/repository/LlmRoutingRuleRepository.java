package pro.datawiki.igaming.llm.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.admin.domain.LlmRoutingRule;
import java.util.List;

@Repository
public interface LlmRoutingRuleRepository extends JpaRepository<LlmRoutingRule, Long> {
    List<LlmRoutingRule> findByActiveTrue();
}
