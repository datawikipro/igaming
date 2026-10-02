package pro.datawiki.igaming.playerfaces;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.service.PlayerAvatarCdnService;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerAvatarCdnServiceTest {

    private PlayerAvatarCdnService cdnService;

    @BeforeEach
    void setUp() {
        cdnService = new PlayerAvatarCdnService();
    }

    @Test
    @DisplayName("Should generate Cyberpunk SVG avatar with gold accents for Top-5 seed")
    void testGenerateCyberpunkSvgTopSeed() {
        String svg = cdnService.generateCyberpunkSvg("Novak Djokovic", SportType.TENNIS, 1, 1);

        assertThat(svg).isNotNull();
        assertThat(svg).contains("<svg");
        assertThat(svg).contains("ND"); // Initials monogram
        assertThat(svg).contains("DJOKOVIC");
        assertThat(svg).contains("#FFD700"); // Gold neon glow
        assertThat(svg).contains("[1]"); // Seed badge
        assertThat(svg).contains("</svg>");
    }

    @Test
    @DisplayName("Should generate Cyberpunk SVG avatar with cyan accents for non-top seeds")
    void testGenerateCyberpunkSvgNonTopSeed() {
        String svg = cdnService.generateCyberpunkSvg("Taylor Fritz", SportType.TENNIS, 12, 12);

        assertThat(svg).isNotNull();
        assertThat(svg).contains("<svg");
        assertThat(svg).contains("TF");
        assertThat(svg).contains("FRITZ");
        assertThat(svg).contains("#00F0FF"); // Cyan neon glow
        assertThat(svg).contains("[12]");
    }

    @Test
    @DisplayName("Should generate Combat glove motif for MMA athletes")
    void testGenerateCyberpunkSvgMma() {
        String svg = cdnService.generateCyberpunkSvg("Islam Makhachev", SportType.MMA, 1, 1);

        assertThat(svg).isNotNull();
        assertThat(svg).contains("IM");
        assertThat(svg).contains("MAKHACHEV");
        assertThat(svg).contains("Combat Glove Icon");
    }

    @Test
    @DisplayName("Should cache and serve custom binary asset")
    void testCacheAndServeCustomAsset() {
        byte[] mockWebp = "RIFF....WEBPVP8 ...".getBytes(StandardCharsets.UTF_8);
        cdnService.cacheImage("alcaraz-final.webp", mockWebp, "image/webp");

        assertThat(cdnService.hasAsset("alcaraz-final.webp")).isTrue();
        assertThat(cdnService.getAvatar("alcaraz-final.webp")).isEqualTo(mockWebp);
        assertThat(cdnService.getContentType("alcaraz-final.webp")).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("Should automatically synthesize SVG avatar if asset is not cached")
    void testSynthesizeFallbackOnMissingAsset() {
        byte[] result = cdnService.getAvatar("jannik-sinner.svg");

        assertThat(result).isNotNull();
        String content = new String(result, StandardCharsets.UTF_8);
        assertThat(content).contains("<svg");
        assertThat(content).contains("JS");
    }
}
