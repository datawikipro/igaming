package pro.datawiki.igaming.llm.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.admin.domain.LlmQueueLink;
import java.util.List;

@Repository
public interface LlmQueueLinkRepository extends JpaRepository<LlmQueueLink, Long> {
    List<LlmQueueLink> findByActiveTrue();
}
