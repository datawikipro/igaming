package pro.datawiki.igaming.analytics.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MdmUiControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MdmUiController controller = new MdmUiController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testRootRedirectsToMdmHub() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/entity-resolution-hub.html"));
    }

    @Test
    void testMdmRedirectsToMdmHub() throws Exception {
        mockMvc.perform(get("/mdm"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/entity-resolution-hub.html"));

        mockMvc.perform(get("/mdm/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/entity-resolution-hub.html"));
    }

    @Test
    void testCrawlerOpsRedirectsToDashboard() throws Exception {
        mockMvc.perform(get("/crawler-ops"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/crawler-ops-dashboard.html"));

        mockMvc.perform(get("/crawler-ops/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/crawler-ops-dashboard.html"));
    }

    @Test
    void testPipelineRedirectsToDashboard() throws Exception {
        mockMvc.perform(get("/pipeline"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/crawler-ops-dashboard.html"));

        mockMvc.perform(get("/pipeline/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/crawler-ops-dashboard.html"));
    }
}
