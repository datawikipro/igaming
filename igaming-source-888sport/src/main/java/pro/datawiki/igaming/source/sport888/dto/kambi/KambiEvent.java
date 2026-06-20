package pro.datawiki.igaming.source.sport888.dto.kambi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KambiEvent {
    private Long id;
    private String name;
    private String homeName;
    private String awayName;
    private String start;
    private String state;
    private String group;
    private List<KambiPath> path;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHomeName() { return homeName; }
    public void setHomeName(String homeName) { this.homeName = homeName; }
    public String getAwayName() { return awayName; }
    public void setAwayName(String awayName) { this.awayName = awayName; }
    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
    public List<KambiPath> getPath() { return path; }
    public void setPath(List<KambiPath> path) { this.path = path; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KambiPath {
        private Long id;
        private String name;
        private String termKey;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTermKey() { return termKey; }
        public void setTermKey(String termKey) { this.termKey = termKey; }
    }
}
