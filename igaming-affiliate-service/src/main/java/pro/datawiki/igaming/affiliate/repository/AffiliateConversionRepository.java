package pro.datawiki.igaming.affiliate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.affiliate.model.AffiliateConversion;
import java.util.List;

@Repository
public interface AffiliateConversionRepository extends JpaRepository<AffiliateConversion, Long> {
    List<AffiliateConversion> findByClickId(String clickId);
    List<AffiliateConversion> findByBookmakerId(String bookmakerId);
    long countByBookmakerId(String bookmakerId);
}
