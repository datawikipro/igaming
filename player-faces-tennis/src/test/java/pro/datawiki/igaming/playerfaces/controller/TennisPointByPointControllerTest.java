package pro.datawiki.igaming.playerfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TennisPointByPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/tennis/stats/point-by-point/{playerName} returns surface point-by-point stats")
    void testGetPointByPointStats() throws Exception {
        mockMvc.perform(get("/api/v1/tennis/stats/point-by-point/Carlos Alcaraz")
                        .param("tour", "ATP")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerName", is("Carlos Alcaraz")))
                .andExpect(jsonPath("$.surfaceStats.HARD.serveStats.firstServeWonPct", notNullValue()))
                .andExpect(jsonPath("$.surfaceStats.CLAY.returnStats.breakPointConversionPct", notNullValue()))
                .andExpect(jsonPath("$.surfaceStats.GRASS.tiebreakStats.tiebreakWinPct", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/tennis/stats/harvest triggers on-demand harvest and returns event")
    void testTriggerHarvest() throws Exception {
        mockMvc.perform(post("/api/v1/tennis/stats/harvest")
                        .param("playerName", "Jannik Sinner")
                        .param("tour", "ATP")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerName", is("Jannik Sinner")))
                .andExpect(jsonPath("$.surfaceStats.HARD", notNullValue()))
                .andExpect(jsonPath("$.sourceProvider", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/tennis/stats/match-edge evaluates surface edge between two players")
    void testEvaluateMatchEdge() throws Exception {
        TennisPointByPointController.MatchEdgeRequest request = new TennisPointByPointController.MatchEdgeRequest(
                "Carlos Alcaraz",
                "Jannik Sinner",
                "Grass",
                "ATP"
        );

        mockMvc.perform(post("/api/v1/tennis/stats/match-edge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surface", is("GRASS")))
                .andExpect(jsonPath("$.p1HoldProbability", notNullValue()))
                .andExpect(jsonPath("$.p2HoldProbability", notNullValue()))
                .andExpect(jsonPath("$.surfaceEdge", notNullValue()));
    }
}
