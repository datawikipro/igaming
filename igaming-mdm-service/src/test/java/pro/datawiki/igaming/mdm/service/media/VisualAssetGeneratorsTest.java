package pro.datawiki.igaming.mdm.service.media;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import pro.datawiki.igaming.mdm.service.generator.AthleteAvatarGenerator;
import pro.datawiki.igaming.mdm.service.generator.TeamLogoGenerator;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VisualAssetGeneratorsTest {

    private final TeamLogoGenerator logoGenerator = new TeamLogoGenerator();
    private final AthleteAvatarGenerator avatarGenerator = new AthleteAvatarGenerator();

    @Test
    void testTeamLogoSvgCrestGeneration() {
        String svg = logoGenerator.generateSvgCrest("Real Madrid", "FOOTBALL");

        assertNotNull(svg);
        assertTrue(svg.startsWith("<svg"), "Should start with <svg root element");
        assertTrue(svg.contains("</svg>"), "Should close </svg>");
        assertTrue(svg.contains("RM"), "Monogram for Real Madrid should be RM");
        assertTrue(svg.contains("⚽"), "Sport emblem should be soccer ball");

        String dataUrl = logoGenerator.generateDataUrl("FC Barcelona", "FOOTBALL");
        assertNotNull(dataUrl);
        assertTrue(dataUrl.startsWith("data:image/svg+xml;base64,"));
    }

    @Test
    void testAthleteAvatarSvgGeneration() {
        String svg = avatarGenerator.generateSvgAvatar("Kylian Mbappe", 7, "FOOTBALL", "🇫🇷");

        assertNotNull(svg);
        assertTrue(svg.startsWith("<svg"), "Should start with <svg root element");
        assertTrue(svg.contains("</svg>"), "Should close </svg>");
        assertTrue(svg.contains(">7<"), "Should contain jersey number 7");
        assertTrue(svg.contains("🇫🇷"), "Should contain French flag");

        // Test with null number fallback to initials
        String svgInitials = avatarGenerator.generateSvgAvatar("Lionel Messi", null, "FOOTBALL", "🇦🇷");
        assertTrue(svgInitials.contains("LM"), "Should contain initials LM when shirt number is null");

        String dataUrl = avatarGenerator.generateDataUrl("Novak Djokovic", null, "TENNIS", "🇷🇸");
        assertNotNull(dataUrl);
        assertTrue(dataUrl.startsWith("data:image/svg+xml;base64,"));
    }

    @Test
    void testVisualAssetStorageReadWrite(@TempDir Path tempDir) {
        VisualAssetStorageService storageService = new VisualAssetStorageService();
        ReflectionTestUtils.setField(storageService, "storageDirProperty", tempDir.toString());
        storageService.init();

        byte[] dummyPng = new byte[]{1, 2, 3, 4, 5};
        storageService.saveAsset("teams", 42L, "png", dummyPng);

        Optional<VisualAssetStorageService.MediaAsset> loaded = storageService.findAsset("teams", 42L);
        assertTrue(loaded.isPresent(), "Saved asset should be present on disk");
        assertEquals("image/png", loaded.get().contentType());
        assertEquals("png", loaded.get().extension());
        assertArrayEquals(dummyPng, loaded.get().data());

        // Check missing ID
        Optional<VisualAssetStorageService.MediaAsset> missing = storageService.findAsset("teams", 999999L);
        assertTrue(missing.isEmpty());
    }
}
