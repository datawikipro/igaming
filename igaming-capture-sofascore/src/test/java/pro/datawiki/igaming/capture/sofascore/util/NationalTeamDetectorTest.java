package pro.datawiki.igaming.capture.sofascore.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NationalTeamDetector}.
 * Covers both API-based detection and name-based heuristics.
 */
class NationalTeamDetectorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────────
    // isNationalTeamFromApi
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("API detection: team.national=true → national team")
    void apiFlag_true_returnsTrue() {
        ObjectNode teamNode = mapper.createObjectNode();
        teamNode.put("id", "123");
        teamNode.put("name", "Germany");
        teamNode.put("national", true);

        assertThat(NationalTeamDetector.isNationalTeamFromApi(teamNode)).isTrue();
    }

    @Test
    @DisplayName("API detection: team.national=false → not national team")
    void apiFlag_false_returnsFalse() {
        ObjectNode teamNode = mapper.createObjectNode();
        teamNode.put("id", "456");
        teamNode.put("name", "Bayern München");
        teamNode.put("national", false);

        assertThat(NationalTeamDetector.isNationalTeamFromApi(teamNode)).isFalse();
    }

    @Test
    @DisplayName("API detection: type='national' → national team")
    void apiType_national_returnsTrue() {
        ObjectNode teamNode = mapper.createObjectNode();
        teamNode.put("id", "789");
        teamNode.put("name", "France");
        teamNode.put("type", "national");

        assertThat(NationalTeamDetector.isNationalTeamFromApi(teamNode)).isTrue();
    }

    @Test
    @DisplayName("API detection: null node → false")
    void apiFlag_nullNode_returnsFalse() {
        assertThat(NationalTeamDetector.isNationalTeamFromApi(null)).isFalse();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // isNationalTeamByName
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Name detection: exact country name → national team")
    void nameDetection_exactCountry_returnsTrue() {
        assertThat(NationalTeamDetector.isNationalTeamByName("France")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("Germany")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("Brazil")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("Russia")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("Japan")).isTrue();
    }

    @Test
    @DisplayName("Name detection: Russian country names → national team")
    void nameDetection_russianCountryNames_returnsTrue() {
        assertThat(NationalTeamDetector.isNationalTeamByName("Германия")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("Франция")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("Бразилия")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("Испания")).isTrue();
    }

    @Test
    @DisplayName("Name detection: country + suffix → national team")
    void nameDetection_countryWithSuffix_returnsTrue() {
        assertThat(NationalTeamDetector.isNationalTeamByName("France U21")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("England U20")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("Brazil Olympic")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("Germany National")).isTrue();
        assertThat(NationalTeamDetector.isNationalTeamByName("Argentina Women")).isTrue();
    }

    @Test
    @DisplayName("Name detection: club team → not national team")
    void nameDetection_clubTeam_returnsFalse() {
        assertThat(NationalTeamDetector.isNationalTeamByName("Bayern München")).isFalse();
        assertThat(NationalTeamDetector.isNationalTeamByName("Manchester United")).isFalse();
        assertThat(NationalTeamDetector.isNationalTeamByName("Real Madrid")).isFalse();
        assertThat(NationalTeamDetector.isNationalTeamByName("FC Barcelona")).isFalse();
    }

    @Test
    @DisplayName("Name detection: blank/null → false")
    void nameDetection_blankOrNull_returnsFalse() {
        assertThat(NationalTeamDetector.isNationalTeamByName(null)).isFalse();
        assertThat(NationalTeamDetector.isNationalTeamByName("")).isFalse();
        assertThat(NationalTeamDetector.isNationalTeamByName("  ")).isFalse();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // resolveCountryCode
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ISO resolution: category.alpha2 takes priority")
    void resolveCountryCode_categoryAlpha2_prioritized() {
        ObjectNode teamNode = mapper.createObjectNode();
        teamNode.put("name", "Germany");

        ObjectNode categoryNode = mapper.createObjectNode();
        categoryNode.put("alpha2", "de");

        String code = NationalTeamDetector.resolveCountryCode(teamNode, "Germany", categoryNode);
        assertThat(code).isEqualTo("DE");
    }

    @Test
    @DisplayName("ISO resolution: team.country.alpha2 as fallback")
    void resolveCountryCode_teamCountryAlpha2_fallback() {
        ObjectNode teamNode = mapper.createObjectNode();
        teamNode.put("name", "France");
        ObjectNode country = mapper.createObjectNode();
        country.put("alpha2", "fr");
        teamNode.set("country", country);

        String code = NationalTeamDetector.resolveCountryCode(teamNode, "France", null);
        assertThat(code).isEqualTo("FR");
    }

    @Test
    @DisplayName("ISO resolution: name-based fallback for 'Brazil'")
    void resolveCountryCode_nameLookup_brazil() {
        String code = NationalTeamDetector.resolveCountryCode(null, "Brazil", null);
        assertThat(code).isEqualTo("BR");
    }

    @Test
    @DisplayName("ISO resolution: name-based fallback for 'South Korea'")
    void resolveCountryCode_nameLookup_southKorea() {
        String code = NationalTeamDetector.resolveCountryCode(null, "South Korea", null);
        assertThat(code).isEqualTo("KR");
    }

    @Test
    @DisplayName("ISO resolution: unknown team returns null")
    void resolveCountryCode_unknownTeam_returnsNull() {
        String code = NationalTeamDetector.resolveCountryCode(null, "Ajax Amsterdam", null);
        assertThat(code).isNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildFlagUrl
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buildFlagUrl: generates correct SofaScore URL")
    void buildFlagUrl_generatesCorrectUrl() {
        String url = NationalTeamDetector.buildFlagUrl("4711");
        assertThat(url).isEqualTo("https://api.sofascore.app/api/v1/team/4711/image");
    }

    @Test
    @DisplayName("buildFlagUrl: null teamId returns null")
    void buildFlagUrl_nullId_returnsNull() {
        assertThat(NationalTeamDetector.buildFlagUrl(null)).isNull();
        assertThat(NationalTeamDetector.buildFlagUrl("")).isNull();
    }
}
