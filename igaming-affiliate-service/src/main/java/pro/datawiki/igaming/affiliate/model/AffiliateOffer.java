package pro.datawiki.igaming.affiliate.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "affiliate_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffiliateOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AffiliatePartner partner;

    @Column(name = "bookmaker_id", nullable = false, length = 64)
    private String bookmakerId;

    @Column(name = "offer_name", nullable = false, length = 255)
    private String offerName;

    @Column(name = "model_type", nullable = false, length = 32)
    @Builder.Default
    private String modelType = "CPA"; // CPA, REVSHARE, HYBRID

    @Column(name = "base_rate", precision = 10, scale = 2)
    private BigDecimal baseRate;

    @Column(length = 16)
    @Builder.Default
    private String currency = "RUB";

    @Column(name = "promo_code", length = 64)
    private String promoCode;

    @Column(name = "tracking_url_template", nullable = false, length = 1024)
    private String trackingUrlTemplate;

    @Column(name = "deep_link_supported")
    @Builder.Default
    private Boolean deepLinkSupported = true;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
