package pro.datawiki.igaming.affiliate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.affiliate.model.AffiliatePartner;
import java.util.Optional;

@Repository
public interface AffiliatePartnerRepository extends JpaRepository<AffiliatePartner, Long> {
    Optional<AffiliatePartner> findByBookmakerId(String bookmakerId);
}
