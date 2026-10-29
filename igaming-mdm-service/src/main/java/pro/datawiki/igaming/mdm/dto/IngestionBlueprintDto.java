package pro.datawiki.igaming.mdm.dto;

import lombok.*;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionBlueprintDto implements Serializable {

    private String externalSystemCode;

    @Builder.Default
    private Map<String, CanonicalSportDto> sports = new HashMap<>();

    @Builder.Default
    private Map<String, CanonicalCountryDto> countries = new HashMap<>();

    @Builder.Default
    private Map<String, CanonicalTournamentDto> tournaments = new HashMap<>();

    @Builder.Default
    private Map<String, CanonicalTeamDto> teams = new HashMap<>();

    @Builder.Default
    private List<CanonicalMatchHeaderDto> activeMatches = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CanonicalCountryDto implements Serializable {
        private Integer countryId;
        private String isoCode;
        private String canonicalName;
        private String flagCode;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CanonicalSportDto implements Serializable {
        private Integer sportId;
        private String slug;
        private String domainKey;
        private Boolean isEsports;
        private Boolean isTeamSport;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CanonicalTournamentDto implements Serializable {
        private Long tournamentId;
        private Integer sportId;
        private Integer categoryId;
        private Integer countryId;
        private String countryName;
        private String slug;
        private Long sofascoreId;
        private String canonicalName;
        private String gender;
        private String ageCategory;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CanonicalTeamDto implements Serializable {
        private Long teamId;
        private Integer sportId;
        private Integer categoryId;
        private Integer countryId;
        private String countryName;
        private Long organizationId;
        private String slug;
        private Long sofascoreId;
        private String canonicalName;
        private Boolean isNational;
        private Boolean isIndividual;
        private String gender;
        private String ageCategory;
    }
}
