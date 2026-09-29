package pro.datawiki.igaming.vkdonut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.vkdonut.model.DonutDonor;
import pro.datawiki.igaming.vkdonut.model.DonutStatus;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link DonutDonor} entities.
 */
@Repository
public interface DonutDonorRepository extends JpaRepository<DonutDonor, Long> {

    Optional<DonutDonor> findByVkUserId(Long vkUserId);

    List<DonutDonor> findAllByStatus(DonutStatus status);
}
