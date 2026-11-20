package pro.datawiki.igaming.analyst.ml.vector;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DerbyRivalryDetectorTest {

    @Test
    void testKeywordDerbyMatches() {
        assertThat(DerbyRivalryDetector.isDerbyMatch("Team A", "Team B", "Copenhagen Derby", "Denmark"))
                .isTrue();
        assertThat(DerbyRivalryDetector.isDerbyMatch("Team A", "Team B", "Московское Дерби", "Russia"))
                .isTrue();
        assertThat(DerbyRivalryDetector.isDerbyMatch("Team A", "Team B", "El Clásico", "Spain"))
                .isTrue();
        assertThat(DerbyRivalryDetector.isDerbyMatch("Team A", "Team B", "Superclásico", "Argentina"))
                .isTrue();
    }

    @Test
    void testCalmNonDerbyMatches() {
        assertThat(DerbyRivalryDetector.isDerbyMatch("Arsenal", "Bournemouth", "Premier League", "England"))
                .isFalse();
        assertThat(DerbyRivalryDetector.isDerbyMatch("Bodø/Glimt", "Tromsø", "Eliteserien", "Norway"))
                .isFalse();
    }
}
