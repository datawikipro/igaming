package pro.datawiki.igaming.llm.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.llm.admin.domain.LlmKeyModelSuspension;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LlmKeyModelSuspensionRepository extends JpaRepository<LlmKeyModelSuspension, Long> {

    Optional<LlmKeyModelSuspension> findByKeyIdAndModelName(Long keyId, String modelName);

    @Query("SELECT s FROM LlmKeyModelSuspension s WHERE s.key.id IN :keyIds AND s.modelName = :modelName AND s.suspendedUntil > :now")
    List<LlmKeyModelSuspension> findActiveSuspensions(@Param("keyIds") List<Long> keyIds, @Param("modelName") String modelName, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM LlmKeyModelSuspension s WHERE s.suspendedUntil < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}
