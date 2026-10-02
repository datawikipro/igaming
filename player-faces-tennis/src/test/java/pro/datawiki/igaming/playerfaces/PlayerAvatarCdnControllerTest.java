package pro.datawiki.igaming.playerfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlayerAvatarCdnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /cdn/avatars/{filename} should return image or synthesized SVG with cache headers")
    void testGetAvatarAsset() throws Exception {
        mockMvc.perform(get("/cdn/avatars/novak-djokovic.svg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("max-age=86400")))
                .andExpect(content().string(containsString("<svg")));
    }

    @Test
    @DisplayName("GET /cdn/avatars/svg/{slug} should dynamically generate Cyberpunk SVG")
    void testGetDynamicCyberpunkSvg() throws Exception {
        mockMvc.perform(get("/cdn/avatars/svg/carlos-alcaraz")
                        .param("seed", "1")
                        .param("sport", "TENNIS"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("image/svg+xml")))
                .andExpect(content().string(containsString("CA")))
                .andExpect(content().string(containsString("[1]")));
    }
}
