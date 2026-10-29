package pro.datawiki.igaming.mdm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TournamentStageNormalizerTest {

    @Test
    @DisplayName("Should extract 1/32 finals as ROUND_OF_64 and preserve clean tournament name")
    void testExtractRoundOf64() {
        TournamentStageNormalizer.StageResult res = TournamentStageNormalizer.extract("Кубок Англии. 1/32 финала");
        assertThat(res.cleanTournamentName()).isEqualTo("Кубок Англии");
        assertThat(res.stage()).isEqualTo("ROUND_OF_64");
        assertThat(res.round()).contains("1/32");
    }

    @Test
    @DisplayName("Should extract 1/4 finals as QUARTER_FINAL")
    void testExtractQuarterFinal() {
        TournamentStageNormalizer.StageResult res = TournamentStageNormalizer.extract("Кубок Лиги. 1/4 финала");
        assertThat(res.cleanTournamentName()).isEqualTo("Кубок Лиги");
        assertThat(res.stage()).isEqualTo("QUARTER_FINAL");
        assertThat(res.round()).contains("1/4");
    }

    @Test
    @DisplayName("Should extract 1/2 finals as SEMI_FINAL")
    void testExtractSemiFinal() {
        TournamentStageNormalizer.StageResult res = TournamentStageNormalizer.extract("Лига Чемпионов. 1/2 финала");
        assertThat(res.cleanTournamentName()).isEqualTo("Лига Чемпионов");
        assertThat(res.stage()).isEqualTo("SEMI_FINAL");
        assertThat(res.round()).contains("1/2");
    }

    @Test
    @DisplayName("Should extract Relegation Playoffs as PLAYOFFS")
    void testExtractPlayoffs() {
        TournamentStageNormalizer.StageResult res = TournamentStageNormalizer.extract("2. Liga, Division A, Relegation Playoffs");
        assertThat(res.cleanTournamentName()).isEqualTo("2. Liga, Division A");
        assertThat(res.stage()).isEqualTo("PLAYOFFS");
        assertThat(res.round()).isEqualToIgnoringCase("Relegation Playoffs");
    }

    @Test
    @DisplayName("Should extract Group A as GROUP_STAGE")
    void testExtractGroupStage() {
        TournamentStageNormalizer.StageResult res = TournamentStageNormalizer.extract("U14 Liga Evolución, Women - Group A");
        assertThat(res.cleanTournamentName()).isEqualTo("U14 Liga Evolución, Women");
        assertThat(res.stage()).isEqualTo("GROUP_STAGE");
        assertThat(res.round()).isEqualToIgnoringCase("Group A");
    }

    @Test
    @DisplayName("Should extract Qualification and strip delimiters")
    void testExtractQualification() {
        TournamentStageNormalizer.StageResult res = TournamentStageNormalizer.extract("WTA. 125. Курмайер. Квалификация");
        assertThat(res.cleanTournamentName()).isEqualTo("WTA. 125. Курмайер");
        assertThat(res.stage()).isEqualTo("QUALIFICATION");
        assertThat(res.round()).isEqualToIgnoringCase("Квалификация");
    }

    @Test
    @DisplayName("Should strip bookmaker statistics props suffixes")
    void testStripStatsSuffix() {
        TournamentStageNormalizer.StageResult res = TournamentStageNormalizer.extract("Чемпионат Италии. Серия B. Статистика");
        assertThat(res.cleanTournamentName()).isEqualTo("Чемпионат Италии. Серия B");
    }

    @Test
    @DisplayName("Should not corrupt standalone tournament names")
    void testCleanTournamentPreserved() {
        TournamentStageNormalizer.StageResult res = TournamentStageNormalizer.extract("Premier League");
        assertThat(res.cleanTournamentName()).isEqualTo("Premier League");
        assertThat(res.stage()).isNull();
        assertThat(res.round()).isNull();
    }
}
