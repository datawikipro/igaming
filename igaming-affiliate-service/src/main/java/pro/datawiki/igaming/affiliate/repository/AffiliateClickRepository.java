package pro.datawiki.igaming.affiliate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.affiliate.model.AffiliateClick;
import java.util.Optional;

@Repository
public interface AffiliateClickRepository extends JpaRepository<AffiliateClick, Long> {
    Optional<AffiliateClick> findByClickId(String clickId);
    long countByBookmakerId(String bookmakerId);
}
