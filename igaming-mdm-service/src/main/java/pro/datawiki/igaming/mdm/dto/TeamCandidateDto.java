package pro.datawiki.igaming.mdm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamCandidateDto implements Serializable {
    private Long teamId;
    private String name;
    private Integer sportId;
    private Integer countryId;
    private String countryName;
    private String slug;
    private String gender;
    private String ageCategory;
}
