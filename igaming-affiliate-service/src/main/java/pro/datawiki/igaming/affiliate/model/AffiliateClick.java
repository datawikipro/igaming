package pro.datawiki.igaming.affiliate.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "affiliate_clicks", indexes = {
    @Index(name = "idx_click_id", columnList = "click_id", unique = true),
    @Index(name = "idx_bookmaker_id", columnList = "bookmaker_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffiliateClick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "click_id", nullable = false, unique = true, length = 64)
    private String clickId;

    @Column(name = "bookmaker_id", nullable = false, length = 64)
    private String bookmakerId;

    @Column(name = "offer_id")
    private Long offerId;

    @Column(name = "utm_source", length = 128)
    private String utmSource;

    @Column(name = "utm_medium", length = 128)
    private String utmMedium;

    @Column(name = "utm_campaign", length = 128)
    private String utmCampaign;

    @Column(name = "utm_content", length = 128)
    private String utmContent;

    @Column(name = "utm_term", length = 128)
    private String utmTerm;

    @Column(name = "sub_id", length = 128)
    private String subId;

    @Column(name = "ip_hash", nullable = false, length = 64)
    private String ipHash; // SHA-256 for privacy and fraud audit

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "referer", length = 512)
    private String referer;

    @Column(name = "redirect_url", length = 1024)
    private String redirectUrl;

    @Column(length = 32)
    @Builder.Default
    private String status = "CLICKED"; // CLICKED, CONVERTED

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
