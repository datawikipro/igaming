package pro.datawiki.igaming.capture.sofascore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SofaScore Capture SimilarityUtils Tests")
class SimilarityUtilsTest {

    @Test
    @DisplayName("Should correctly match women teams with gender and tournament context")
    void shouldMatchWomenTeams() {
        assertThat(SimilarityUtils.areTeamsSimilar("Челси (жен)", "Chelsea FC Women")).isTrue();
        assertThat(SimilarityUtils.areTeamsSimilar("Челси (жен)", "Chelsea", "Women's Super League")).isTrue();
        assertThat(SimilarityUtils.areTeamsSimilar("Барселона (жен)", "Barcelona Femení")).isTrue();
    }

    @Test
    @DisplayName("Should strictly prevent cross-gender matching")
    void shouldRejectCrossGenderMatch() {
        assertThat(SimilarityUtils.areTeamsSimilar("Челси", "Chelsea FC Women")).isFalse();
        assertThat(SimilarityUtils.areTeamsSimilar("Челси", "Chelsea", "Women's Super League")).isFalse();
        assertThat(SimilarityUtils.areTeamsSimilar("Челси (жен)", "Chelsea FC")).isFalse();
    }

    @Test
    @DisplayName("Should correctly match youth teams and reject youth vs senior")
    void shouldHandleYouthTeams() {
        assertThat(SimilarityUtils.areTeamsSimilar("Arsenal U19", "Арсенал U19")).isTrue();
        assertThat(SimilarityUtils.areTeamsSimilar("Arsenal", "Arsenal U19")).isFalse();
        assertThat(SimilarityUtils.areTeamsSimilar("Реал Мадрид (мол)", "Real Madrid", "UEFA Youth League")).isTrue();
    }
}
