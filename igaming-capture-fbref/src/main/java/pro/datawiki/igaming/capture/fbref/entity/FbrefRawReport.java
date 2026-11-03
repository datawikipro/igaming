package pro.datawiki.igaming.capture.fbref.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Raw FBref report entity mapped to local table fbref_raw_reports.
 */
@Entity
@Table(name = "fbref_raw_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbrefRawReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private String matchId;

    @Column(name = "external_match_id")
    private String externalMatchId;

    @Column(name = "match_url")
    private String matchUrl;

    @Column(name = "competition")
    private String competition;

    @Column(name = "season")
    private String season;

    @Column(name = "home_team")
    private String homeTeam;

    @Column(name = "away_team")
    private String awayTeam;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "home_xg")
    private Double homeXg;

    @Column(name = "away_xg")
    private Double awayXg;

    @Column(name = "home_xag")
    private Double homeXag;

    @Column(name = "away_xag")
    private Double awayXag;

    @Column(name = "home_sca")
    private Integer homeSca;

    @Column(name = "away_sca")
    private Integer awaySca;

    @Column(name = "home_gca")
    private Integer homeGca;

    @Column(name = "away_gca")
    private Integer awayGca;

    @Column(name = "home_npxg")
    private Double homeNpxg;

    @Column(name = "away_npxg")
    private Double awayNpxg;

    @Column(name = "status")
    private String status;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
