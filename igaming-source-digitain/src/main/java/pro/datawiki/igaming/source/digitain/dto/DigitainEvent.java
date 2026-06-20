package pro.datawiki.igaming.source.digitain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitainEvent {

    @JsonProperty("Id")
    private Long id;

    @JsonProperty("N")
    private String name;

    @JsonProperty("EGN")
    private String englishName;

    @JsonProperty("HT")
    private String homeTeam;

    @JsonProperty("AT")
    private String awayTeam;

    @JsonProperty("EHT")
    private String englishHomeTeam;

    @JsonProperty("EAT")
    private String englishAwayTeam;

    @JsonProperty("SId")
    private Long sportId;

    @JsonProperty("SN")
    private String sportName;

    @JsonProperty("ESN")
    private String englishSportName;

    @JsonProperty("D")
    private String date;

    @JsonProperty("CId")
    private Long championshipId;

    @JsonProperty("CN")
    private String championshipName;

    @JsonProperty("ECN")
    private String englishChampionshipName;

    @JsonProperty("StakeTypes")
    private List<DigitainStakeGroup> stakeTypes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEnglishName() { return englishName; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }
    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }
    public String getEnglishHomeTeam() { return englishHomeTeam; }
    public void setEnglishHomeTeam(String englishHomeTeam) { this.englishHomeTeam = englishHomeTeam; }
    public String getEnglishAwayTeam() { return englishAwayTeam; }
    public void setEnglishAwayTeam(String englishAwayTeam) { this.englishAwayTeam = englishAwayTeam; }
    public Long getSportId() { return sportId; }
    public void setSportId(Long sportId) { this.sportId = sportId; }
    public String getSportName() { return sportName; }
    public void setSportName(String sportName) { this.sportName = sportName; }
    public String getEnglishSportName() { return englishSportName; }
    public void setEnglishSportName(String englishSportName) { this.englishSportName = englishSportName; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Long getChampionshipId() { return championshipId; }
    public void setChampionshipId(Long championshipId) { this.championshipId = championshipId; }
    public String getChampionshipName() { return championshipName; }
    public void setChampionshipName(String championshipName) { this.championshipName = championshipName; }
    public String getEnglishChampionshipName() { return englishChampionshipName; }
    public void setEnglishChampionshipName(String englishChampionshipName) { this.englishChampionshipName = englishChampionshipName; }
    public List<DigitainStakeGroup> getStakeTypes() { return stakeTypes; }
    public void setStakeTypes(List<DigitainStakeGroup> stakeTypes) { this.stakeTypes = stakeTypes; }
}
