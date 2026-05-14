package pro.datawiki.igaming.llm.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.admin.domain.LlmProviderKey;

import java.util.List;
import java.util.Optional;

@Repository
public interface LlmProviderKeyRepository extends JpaRepository<LlmProviderKey, Long> {

    List<LlmProviderKey> findByProviderIdAndActiveTrue(Long providerId);

    /**
     * Round-robin: следующий активный ключ после указанного id.
     * Если нет ключей после — вернёт первый (циклически).
     */
    @Query("""
        SELECT k FROM LlmProviderKey k
        WHERE k.provider.id = :providerId AND k.active = true AND k.id > :afterId
        ORDER BY k.id ASC
        LIMIT 1
    """)
    Optional<LlmProviderKey> findNextKey(@Param("providerId") Long providerId, @Param("afterId") Long afterId);

    @Query("""
        SELECT k FROM LlmProviderKey k
        WHERE k.provider.id = :providerId AND k.active = true
        ORDER BY k.id ASC
        LIMIT 1
    """)
    Optional<LlmProviderKey> findFirstActiveKey(@Param("providerId") Long providerId);
}
