package pro.datawiki.igaming.affiliate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.affiliate.model.AffiliateOffer;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateOfferRepository extends JpaRepository<AffiliateOffer, Long> {
    Optional<AffiliateOffer> findFirstByBookmakerIdAndIsActiveTrue(String bookmakerId);
    List<AffiliateOffer> findAllByIsActiveTrue();
    List<AffiliateOffer> findByBookmakerId(String bookmakerId);
}
