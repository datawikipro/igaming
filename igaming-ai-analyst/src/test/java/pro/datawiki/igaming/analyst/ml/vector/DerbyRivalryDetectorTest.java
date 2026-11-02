package pro.datawiki.igaming.analyst.ml.vector;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DerbyRivalryDetectorTest {

    @Test
    void testCluster2MatchesIdentifiedAsDerby() {
        // 1. Helmond Sport vs FC Eindhoven (Brabant Derby, Netherlands)
        assertThat(DerbyRivalryDetector.isDerbyMatch("Helmond Sport", "FC Eindhoven", "Eerste Divisie", "Netherlands"))
                .isTrue();

        // 2. St. Patrick's Athletic vs Bohemian FC (Dublin Derby, Ireland)
        assertThat(DerbyRivalryDetector.isDerbyMatch("St. Patrick's", "Bohemian FC", "Premier Division", "Ireland"))
                .isTrue();

        // 3. Sportivo Trinidense vs Rubio Ñu (Paraguay Rivalry)
        assertThat(DerbyRivalryDetector.isDerbyMatch("Sportivo Trinidense", "Rubio Ñu", "Division Profesional", "Paraguay"))
                .isTrue();

        // 4. Inter San Carlos vs Escorpiones (Costa Rica Rivalry)
        assertThat(DerbyRivalryDetector.isDerbyMatch("Inter San Carlos", "Escorpiones", "Liga de Ascenso", "Costa Rica"))
                .isTrue();

        // 5. Skive vs VSK Aarhus (Jutland Derby, Denmark)
        assertThat(DerbyRivalryDetector.isDerbyMatch("Skive", "VSK Aarhus", "2nd Division", "Denmark"))
                .isTrue();

        // 6. Miedź II vs Warta Gorzów (Poland Regional Clash)
        assertThat(DerbyRivalryDetector.isDerbyMatch("Miedź II", "Warta Gorzów", "III Liga", "Poland"))
                .isTrue();
    }

    @Test
    void testKeywordDerbyMatches() {
        assertThat(DerbyRivalryDetector.isDerbyMatch("Team A", "Team B", "Copenhagen Derby", "Denmark"))
                .isTrue();
        assertThat(DerbyRivalryDetector.isDerbyMatch("Team A", "Team B", "Московское Дерби", "Russia"))
                .isTrue();
        assertThat(DerbyRivalryDetector.isDerbyMatch("Team A", "Team B", "El Clásico", "Spain"))
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
