package pro.datawiki.igaming.affiliate.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "affiliate_conversions", indexes = {
    @Index(name = "idx_conv_click_id", columnList = "click_id"),
    @Index(name = "idx_conv_bookmaker_id", columnList = "bookmaker_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffiliateConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "click_id", nullable = false, length = 64)
    private String clickId;

    @Column(name = "bookmaker_id", nullable = false, length = 64)
    private String bookmakerId;

    @Column(name = "conversion_type", nullable = false, length = 64)
    @Builder.Default
    private String conversionType = "REGISTRATION"; // REGISTRATION, FIRST_DEPOSIT, RE_DEPOSIT

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 2)
    private BigDecimal payout;

    @Column(length = 16)
    @Builder.Default
    private String currency = "RUB";

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String status = "APPROVED"; // PENDING, APPROVED, REJECTED

    @Column(name = "postback_payload", columnDefinition = "TEXT")
    private String postbackPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
