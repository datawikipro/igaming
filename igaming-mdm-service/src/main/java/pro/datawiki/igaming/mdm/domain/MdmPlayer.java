package pro.datawiki.igaming.mdm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mdm_player", uniqueConstraints = {
    @UniqueConstraint(name = "uk_mdm_player_sport_slug", columnNames = {"sport_id", "slug"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private MdmSport sport;

    @Column(nullable = false, length = 191)
    private String slug;

    @Column(name = "sofascore_id", unique = true)
    private Long sofascoreId;

    @Column(name = "first_name", length = 128)
    private String firstName;

    @Column(name = "last_name", length = 128)
    private String lastName;

    @Column(name = "common_name", nullable = false, length = 191)
    private String commonName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizenship_category_id")
    private MdmCategory citizenshipCategory;

    /**
     * Authoritative nationality via FK to mdm_country.
     * Used to render nationality flags (e.g. 🇧🇷, 🇩🇪) in player cards.
     * Populated from citizenship_category_id on backfill; updated by SofaScore enrichment.
     * Null until resolved (e.g. for unknown/bot players).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality_country_id")
    private MdmCountry nationality;

    @Column(name = "primary_position", length = 32)
    private String primaryPosition;

    @Column(name = "photo_url", length = 512)
    private String photoUrl;

    @Builder.Default
    @Column(nullable = false, length = 16)
    private String gender = "MALE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
