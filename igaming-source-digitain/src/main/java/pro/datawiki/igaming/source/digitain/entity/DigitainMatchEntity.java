package pro.datawiki.igaming.source.digitain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "digitain_matches")
public class DigitainMatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false)
    private String externalId;

    @Column(name = "sport_name")
    private String sportName;

    @Column(name = "league_name")
    private String leagueName;

    @Column(name = "team1")
    private String team1;

    @Column(name = "team2")
    private String team2;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "raw_json", columnDefinition = "text")
    private String rawJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getSportName() { return sportName; }
    public void setSportName(String sportName) { this.sportName = sportName; }
    public String getLeagueName() { return leagueName; }
    public void setLeagueName(String leagueName) { this.leagueName = leagueName; }
    public String getTeam1() { return team1; }
    public void setTeam1(String team1) { this.team1 = team1; }
    public String getTeam2() { return team2; }
    public void setTeam2(String team2) { this.team2 = team2; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }
}
