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
 * Raw Understat shot row entity mapped to local table understat_raw_shots.
 */
@Entity
@Table(name = "understat_raw_shots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnderstatRawShot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private String matchId;

    @Column(name = "external_shot_id")
    private String externalShotId;

    @Column(name = "minute")
    private Integer minute;

    @Column(name = "result")
    private String result;

    @Column(name = "x_coord")
    private Double xCoord;

    @Column(name = "y_coord")
    private Double yCoord;

    @Column(name = "xg")
    private Double xg;

    @Column(name = "player")
    private String player;

    @Column(name = "team_side")
    private String teamSide;

    @Column(name = "situation")
    private String situation;

    @Column(name = "shot_type")
    private String shotType;

    @Column(name = "last_action")
    private String lastAction;

    @Column(name = "status")
    private String status;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
