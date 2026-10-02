package pro.datawiki.igaming.playerfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerFaceNormalizationTest {

    @Test
    @DisplayName("Should normalize Slavic names with diacritics and special characters")
    void testNormalizeDiacritics() {
        assertEquals("novak djokovic", PlayerFace.normalizeName("Novak Đoković"));
        assertEquals("iga swiatek", PlayerFace.normalizeName("Iga Świątek"));
        assertEquals("alexander zverev", PlayerFace.normalizeName("Alexander   Zverev "));
        assertEquals("carlos alcaraz", PlayerFace.normalizeName("Carlos Alcaraz"));
        assertEquals("jannik sinner", PlayerFace.normalizeName("Jannik Sinner"));
        assertEquals("islam makhachev", PlayerFace.normalizeName("Islam Makhachev"));
    }

    @Test
    @DisplayName("Should handle edge cases like null, blanks and special symbols")
    void testEdgeCases() {
        assertEquals("", PlayerFace.normalizeName(null));
        assertEquals("", PlayerFace.normalizeName("   "));
        assertEquals("bjorn borg", PlayerFace.normalizeName("Björn Borg"));
        assertEquals("stan wawrinka", PlayerFace.normalizeName("Stan Wawrinka (SUI)"));
    }
}
