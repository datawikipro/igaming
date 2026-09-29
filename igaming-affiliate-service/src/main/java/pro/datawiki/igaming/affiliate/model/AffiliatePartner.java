package pro.datawiki.igaming.affiliate.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "affiliate_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AffiliatePartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bookmaker_id", nullable = false, unique = true, length = 64)
    private String bookmakerId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "network_name", nullable = false, length = 128)
    private String networkName;

    @Column(name = "affiliate_manager_contact", length = 255)
    private String affiliateManagerContact;

    @Column(name = "website_url", length = 512)
    private String websiteUrl;

    @Column(name = "login_url", length = 512)
    private String loginUrl;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, PENDING, REJECTED

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
