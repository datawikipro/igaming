package pro.datawiki.igaming.analyst.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.analyst.dto.*;
import pro.datawiki.igaming.analyst.entity.MatchVisionBlueprint;
import pro.datawiki.igaming.analyst.repository.MatchVisionBlueprintRepository;
import pro.datawiki.igaming.analyst.service.MatchVisionReconciler;
import pro.datawiki.igaming.analyst.service.MatchVisionService;
import pro.datawiki.igaming.analyst.service.TacticalNarrativeEngine;
import pro.datawiki.igaming.analyst.service.TacticalNarrativeEngine.Tone;
import pro.datawiki.igaming.dto.sport.Sports;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MatchVisionIntegrationTest {

    private ObjectMapper objectMapper;
    private LineupPredictorService lineupPredictorService;
    private MatchVisionBlueprintRepository blueprintRepository;
    private MatchVisionService matchVisionService;
    private MatchVisionReconciler reconciler;
    private TacticalNarrativeEngine narrativeEngine;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        lineupPredictorService = new LineupPredictorService(objectMapper);
        blueprintRepository = Mockito.mock(MatchVisionBlueprintRepository.class);
        narrativeEngine = new TacticalNarrativeEngine();
        matchVisionService = new MatchVisionService(objectMapper, lineupPredictorService, blueprintRepository, narrativeEngine);
        reconciler = new MatchVisionReconciler(objectMapper, blueprintRepository);
    }

    @Test
    @DisplayName("LineupPredictorService correctly predicts formation, starters, and bench jokers")
    void testLineupPrediction() {
        // Arrange: 3 historical match lineups
        List<Map<String, Object>> history = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("home_team", "Team Alpha");
            row.put("lineups_json", """
            {
                "home": {
                    "formation": "4-3-3",
                    "players": [
                        {"player": {"name": "Goalie One", "position": "G", "jerseyNumber": 1}, "substitute": false, "statistics": {"rating": 7.2}},
                        {"player": {"name": "Def One", "position": "D", "jerseyNumber": 2}, "substitute": false, "statistics": {"rating": 6.9}},
                        {"player": {"name": "Def Two", "position": "D", "jerseyNumber": 3}, "substitute": false, "statistics": {"rating": 6.8}},
                        {"player": {"name": "Def Three", "position": "D", "jerseyNumber": 4}, "substitute": false, "statistics": {"rating": 6.9}},
                        {"player": {"name": "Def Four", "position": "D", "jerseyNumber": 5}, "substitute": false, "statistics": {"rating": 6.7}},
                        {"player": {"name": "Mid One", "position": "M", "jerseyNumber": 6}, "substitute": false, "statistics": {"rating": 7.0}},
                        {"player": {"name": "Mid Two", "position": "M", "jerseyNumber": 8}, "substitute": false, "statistics": {"rating": 7.1}},
                        {"player": {"name": "Mid Three", "position": "M", "jerseyNumber": 10}, "substitute": false, "statistics": {"rating": 7.4}},
                        {"player": {"name": "Fwd One", "position": "F", "jerseyNumber": 7}, "substitute": false, "statistics": {"rating": 7.3}},
                        {"player": {"name": "Fwd Two", "position": "F", "jerseyNumber": 9}, "substitute": false, "statistics": {"rating": 7.5}},
                        {"player": {"name": "Fwd Three", "position": "F", "jerseyNumber": 11}, "substitute": false, "statistics": {"rating": 7.1}},
                        {"player": {"name": "Super Sub", "position": "F", "jerseyNumber": 19}, "substitute": true, "statistics": {"rating": 7.2}}
                    ],
                    "missingPlayers": [
                        {"player": {"name": "Broken Knee"}, "reason": "Cruciate ligament"}
                    ]
                }
            }
            """);
            history.add(row);
        }

        // Act
        ProjectedLineupDto lineup = lineupPredictorService.forecastLineup("Team Alpha", history);

        // Assert
        assertNotNull(lineup);
        assertEquals("4-3-3", lineup.getFormation());
        assertEquals(11, lineup.getStarters().size());
        assertTrue(lineup.getStarters().stream().anyMatch(p -> "Goalie One".equals(p.getName())));
        assertTrue(lineup.getStarters().stream().anyMatch(p -> "Fwd Two".equals(p.getName())));

        // Super Sub should be in bench as joker
        assertTrue(lineup.getBench().stream().anyMatch(p -> "Super Sub".equals(p.getName()) && p.isJoker()));

        // Injured player must be excluded
        assertFalse(lineup.getStarters().stream().anyMatch(p -> "Broken Knee".equals(p.getName())));
    }

    @Test
    @DisplayName("MatchVisionService builds 45-parameter tactical matrix and counterfactual branches")
    void testMatchVisionGeneration() {
        // Arrange
        when(blueprintRepository.findByMatchId(any())).thenReturn(Optional.empty());

        // Act
        MatchVisionBlueprintDto dto = matchVisionService.buildAndPersistBlueprint(
                12345L,
                "football",
                "Team Alpha",
                "Team Beta",
                "Champions League",
                LocalDateTime.now(),
                BigDecimal.valueOf(0.33),
                BigDecimal.valueOf(0.34),
                BigDecimal.valueOf(0.33),
                List.of(),
                List.of()
        );

        // Assert
        assertNotNull(dto);
        assertEquals("FOOTBALL", dto.getSport());
        assertEquals(45, dto.getTacticalMatrix().size());
        assertEquals(3, dto.getScenarios().size()); // 3 counterfactual branches
        assertTrue(dto.getMostLikelyScores().contains("1:1"));

        // Generate narrative
        String narrative = narrativeEngine.generatePreMatchNarrative(dto);
        assertNotNull(narrative);
        assertTrue(narrative.contains("The Athletic / Opta"));
        assertTrue(narrative.contains("Team Alpha — Team Beta"));
        assertTrue(narrative.contains("АКТ 1"));
        assertTrue(narrative.contains("АКТ 2"));
        assertTrue(narrative.contains("АКТ 3"));
    }

    @Test
    @DisplayName("Dual Tone and Multilingual Narrative Generation (RU, EN, ES, FR, PT)")
    void testDualToneAndMultilingualNarrative() {
        // Arrange
        MatchVisionBlueprintDto blueprint = MatchVisionBlueprintDto.builder()
                .matchId(777L)
                .homeTeam("Real Madrid")
                .awayTeam("Manchester City")
                .tournament("UEFA Champions League")
                .probHome(BigDecimal.valueOf(0.38))
                .probDraw(BigDecimal.valueOf(0.30))
                .probAway(BigDecimal.valueOf(0.32))
                .mostLikelyScores(List.of("1:1", "2:1", "1:2"))
                .homeProjectedLineup(ProjectedLineupDto.builder()
                        .teamName("Real Madrid")
                        .formation("4-3-3")
                        .confidencePct(88.0)
                        .starters(List.of(
                                ProjectedPlayerDto.builder().name("Courtois").position("G").rating(7.4).build(),
                                ProjectedPlayerDto.builder().name("Vinicius Jr").position("F").rating(8.1).build()
                        ))
                        .bench(List.of(
                                ProjectedPlayerDto.builder().name("Brahim Diaz").position("M").rating(7.3).isJoker(true).build()
                        ))
                        .contestedDuels(List.of("RW: Rodrygo vs Brahim"))
                        .build())
                .awayProjectedLineup(ProjectedLineupDto.builder()
                        .teamName("Manchester City")
                        .formation("4-1-4-1")
                        .confidencePct(85.0)
                        .starters(List.of(
                                ProjectedPlayerDto.builder().name("Ederson").position("G").rating(7.2).build(),
                                ProjectedPlayerDto.builder().name("Haaland").position("F").rating(8.2).build()
                        ))
                        .bench(List.of(
                                ProjectedPlayerDto.builder().name("Doku").position("F").rating(7.2).isJoker(true).build()
                        ))
                        .contestedDuels(List.of("AM: Foden vs Alvarez"))
                        .build())
                .tacticalMatrix(List.of(
                        TacticalMatrixPredictionDto.builder().metricKey("p1_total_under_1_5").projectedCondition("<= 1.5").build()
                ))
                .scenarios(List.of(
                        Map.of("condition", "0:0 at 60'", "game_script", "High block pressing")
                ))
                .build();

        // 1. Test SPORTVIBE_MEDIA across 5 languages
        String mediaRu = narrativeEngine.generatePreMatchNarrative(blueprint, Tone.SPORTVIBE_MEDIA, "ru");
        assertTrue(mediaRu.contains("SportVibe Media Story"));
        assertTrue(mediaRu.contains("Битва стилей"));
        assertTrue(mediaRu.contains("ГЛАВА 1"));
        assertTrue(mediaRu.contains("ГЛАВА 2"));
        assertTrue(mediaRu.contains("E-E-A-T"));

        String mediaEn = narrativeEngine.generatePreMatchNarrative(blueprint, Tone.SPORTVIBE_MEDIA, "en");
        assertTrue(mediaEn.contains("SportVibe Media Story"));
        assertTrue(mediaEn.contains("Clash of Philosophies"));
        assertTrue(mediaEn.contains("CHAPTER 1"));
        assertTrue(mediaEn.contains("CHAPTER 2"));
        assertTrue(mediaEn.contains("E-E-A-T"));

        String mediaEs = narrativeEngine.generatePreMatchNarrative(blueprint, Tone.SPORTVIBE_MEDIA, "es");
        assertTrue(mediaEs.contains("SportVibe Media Story"));
        assertTrue(mediaEs.contains("Choque de Estilos"));
        assertTrue(mediaEs.contains("CAPÍTULO 1"));
        assertTrue(mediaEs.contains("E-E-A-T"));

        String mediaFr = narrativeEngine.generatePreMatchNarrative(blueprint, Tone.SPORTVIBE_MEDIA, "fr");
        assertTrue(mediaFr.contains("SportVibe Media Story"));
        assertTrue(mediaFr.contains("Choc des Philosophies"));
        assertTrue(mediaFr.contains("CHAPITRE 1"));
        assertTrue(mediaFr.contains("E-E-A-T"));

        String mediaPt = narrativeEngine.generatePreMatchNarrative(blueprint, Tone.SPORTVIBE_MEDIA, "pt");
        assertTrue(mediaPt.contains("SportVibe Media Story"));
        assertTrue(mediaPt.contains("Choque de Estilos"));
        assertTrue(mediaPt.contains("CAPÍTULO 1"));
        assertTrue(mediaPt.contains("E-E-A-T"));

        // 2. Test SMARTBET_QUANT across 5 languages
        String quantRu = narrativeEngine.generatePreMatchNarrative(blueprint, Tone.SMARTBET_QUANT, "ru");
        assertTrue(quantRu.contains("ВЕРОЯТНОСТНОЕ ЯДРО ПАРИТЕТА"));
        assertTrue(quantRu.contains("АКТ 1"));
        assertTrue(quantRu.contains("АКТ 2"));
        assertTrue(quantRu.contains("АКТ 3"));

        String quantEn = narrativeEngine.generatePreMatchNarrative(blueprint, Tone.SMARTBET_QUANT, "en");
        assertTrue(quantEn.contains("PROBABILITY CORE & PARITY ENGINE"));
        assertTrue(quantEn.contains("ACT 1"));
        assertTrue(quantEn.contains("ACT 2"));
        assertTrue(quantEn.contains("ACT 3"));

        String quantEs = narrativeEngine.generatePreMatchNarrative(blueprint, Tone.SMARTBET_QUANT, "es");
        assertTrue(quantEs.contains("NÚCLEO PROBABILÍSTICO DE PARIDAD"));
        assertTrue(quantEs.contains("ACTO 1"));
        assertTrue(quantEs.contains("ACTO 2"));
        assertTrue(quantEs.contains("ACTO 3"));

        String quantFr = narrativeEngine.generatePreMatchNarrative(blueprint, Tone.SMARTBET_QUANT, "fr");
        assertTrue(quantFr.contains("CŒUR PROBABILISTE DE PARITÉ"));
        assertTrue(quantFr.contains("ACTE 1"));
        assertTrue(quantFr.contains("ACTE 2"));
        assertTrue(quantFr.contains("ACTE 3"));

        String quantPt = narrativeEngine.generatePreMatchNarrative(blueprint, Tone.SMARTBET_QUANT, "pt");
        assertTrue(quantPt.contains("NÚCLEO PROBABILÍSTICO DE PARIDADE"));
        assertTrue(quantPt.contains("ATO 1"));
        assertTrue(quantPt.contains("ATO 2"));
        assertTrue(quantPt.contains("ATO 3"));
    }

    @Test
    @DisplayName("Dossier Mutation Lifecycle: v1 -> v2 (injury delta) -> v_confirmed")
    void testBlueprintMutationVersioningAndDelta() throws Exception {
        // Arrange
        AtomicReference<MatchVisionBlueprint> entityHolder = new AtomicReference<>();

        ProjectedPlayerDto starterFwd = ProjectedPlayerDto.builder().name("Star Striker").position("F").rating(7.9).startsCount(10).build();
        ProjectedPlayerDto starterMid = ProjectedPlayerDto.builder().name("Key Playmaker").position("M").rating(7.7).startsCount(10).build();
        ProjectedPlayerDto benchFwd = ProjectedPlayerDto.builder().name("Reserve Finisher").position("F").rating(7.2).benchCount(6).build();

        ProjectedLineupDto homeLineup = ProjectedLineupDto.builder()
                .teamName("Arsenal")
                .formation("4-3-3")
                .confidencePct(85.0)
                .starters(new ArrayList<>(List.of(starterFwd, starterMid)))
                .bench(new ArrayList<>(List.of(benchFwd)))
                .build();

        ProjectedLineupDto awayLineup = ProjectedLineupDto.builder()
                .teamName("Chelsea")
                .formation("4-2-3-1")
                .confidencePct(80.0)
                .starters(new ArrayList<>(List.of(ProjectedPlayerDto.builder().name("Cole Palmer").position("M").rating(8.0).build())))
                .bench(new ArrayList<>())
                .build();

        MatchVisionBlueprint initialEntity = MatchVisionBlueprint.builder()
                .matchId(54321L)
                .sportName("FOOTBALL")
                .homeTeam("Arsenal")
                .awayTeam("Chelsea")
                .tournamentName("Premier League")
                .probHome(BigDecimal.valueOf(0.45))
                .probDraw(BigDecimal.valueOf(0.28))
                .probAway(BigDecimal.valueOf(0.27))
                .currentVersion(1)
                .projectedLineupHomeJson(objectMapper.writeValueAsString(homeLineup))
                .projectedLineupAwayJson(objectMapper.writeValueAsString(awayLineup))
                .tacticalMatrixJson("[]")
                .scenariosJson("[]")
                .versionsHistoryJson("[]")
                .status("PROJECTED")
                .build();

        entityHolder.set(initialEntity);

        when(blueprintRepository.findByMatchId(54321L)).thenAnswer(inv -> Optional.of(entityHolder.get()));
        when(blueprintRepository.save(any())).thenAnswer(inv -> {
            MatchVisionBlueprint saved = inv.getArgument(0);
            entityHolder.set(saved);
            return saved;
        });

        // Act 1: Starter injury event -> trigger v2 mutation
        MatchVisionBlueprintDto v2Dto = matchVisionService.mutateBlueprintOnEvent(
                54321L,
                "INJURY",
                "Cruciate ligament injury in training",
                List.of("Star Striker")
        );

        // Assert v2
        assertNotNull(v2Dto);
        assertEquals(2, v2Dto.getCurrentVersion());
        assertEquals("v2", v2Dto.getVersionTag());
        assertNotNull(v2Dto.getDeltaSummary());
        assertTrue(v2Dto.getDeltaSummary().contains("Star Striker"));
        assertTrue(v2Dto.getDeltaSummary().contains("Reserve Finisher"));
        assertTrue(v2Dto.getDeltaSummary().contains("выбыл"));

        // Verify starter lineup updated
        List<ProjectedPlayerDto> v2Starters = v2Dto.getHomeProjectedLineup().getStarters();
        assertTrue(v2Starters.stream().anyMatch(p -> "Reserve Finisher".equals(p.getName())));
        assertFalse(v2Starters.stream().anyMatch(p -> "Star Striker".equals(p.getName())));

        // Verify versions history recorded
        assertNotNull(v2Dto.getVersionsHistory());
        assertEquals(1, v2Dto.getVersionsHistory().size());
        assertEquals("v2", v2Dto.getVersionsHistory().get(0).get("version_tag"));

        // Verify narrative updated with delta
        assertNotNull(v2Dto.getTacticalNarrative());
        assertTrue(v2Dto.getTacticalNarrative().contains("РЕАКТИВНАЯ ДЕЛЬТА СОСТАВА (v2)"));

        // Act 2: Official matchday lineup announcement -> trigger v_confirmed mutation
        MatchVisionBlueprintDto confirmedDto = matchVisionService.mutateBlueprintOnEvent(
                54321L,
                "CONFIRMED_LINEUP",
                "Official Premier League match protocol submitted",
                List.of()
        );

        // Assert v_confirmed
        assertNotNull(confirmedDto);
        assertEquals(3, confirmedDto.getCurrentVersion());
        assertEquals("v_confirmed", confirmedDto.getVersionTag());
        assertEquals("CONFIRMED", confirmedDto.getStatus());
        assertEquals(2, confirmedDto.getVersionsHistory().size());
        assertEquals("v_confirmed", confirmedDto.getVersionsHistory().get(1).get("version_tag"));
    }

    @Test
    @DisplayName("buildLlmEditorialPrompt integrates Google Trends search intent organically")
    void testLlmEditorialPromptWithGoogleTrends() {
        // Arrange
        MatchVisionBlueprintDto blueprint = MatchVisionBlueprintDto.builder()
                .matchId(888L)
                .homeTeam("Bayern Munich")
                .awayTeam("Borussia Dortmund")
                .tournament("Bundesliga")
                .probHome(BigDecimal.valueOf(0.55))
                .probDraw(BigDecimal.valueOf(0.25))
                .probAway(BigDecimal.valueOf(0.20))
                .mostLikelyScores(List.of("2:1", "3:1", "1:1"))
                .homeProjectedLineup(ProjectedLineupDto.builder()
                        .teamName("Bayern Munich")
                        .formation("4-2-3-1")
                        .confidencePct(90.0)
                        .build())
                .awayProjectedLineup(ProjectedLineupDto.builder()
                        .teamName("Borussia Dortmund")
                        .formation("4-3-3")
                        .confidencePct(85.0)
                        .build())
                .versionTag("v2")
                .deltaSummary("«Bayern Munich»: Kane out with ankle sprain -> replaced by Tel")
                .build();

        GoogleTrendsMetricsDto trends = GoogleTrendsMetricsDto.builder()
                .query("Bayern Munich vs Borussia Dortmund Der Klassiker")
                .searchVolumeIndex(94)
                .targetLanguage("en")
                .risingQueries(List.of(
                        "Is Harry Kane starting today vs Dortmund",
                        "Bayern Munich lineup without Kane",
                        "Borussia Dortmund tactical plan Der Klassiker"
                ))
                .topQueries(List.of(
                        "Der Klassiker head to head record",
                        "Bayern vs Dortmund odds today"
                ))
                .build();

        // Act 1: Build prompt for SPORTVIBE_MEDIA in English
        String mediaPrompt = narrativeEngine.buildLlmEditorialPrompt(
                blueprint, trends, "en", Tone.SPORTVIBE_MEDIA, "v2", blueprint.getDeltaSummary()
        );

        // Assert media prompt
        assertNotNull(mediaPrompt);
        assertTrue(mediaPrompt.contains("SportVibe Media"));
        assertTrue(mediaPrompt.contains("Google E-E-A-T"));
        assertTrue(mediaPrompt.contains("MANDATORY LANGUAGE DIRECTIVE:"));
        assertTrue(mediaPrompt.contains("English (en)"));
        assertTrue(mediaPrompt.contains("GOOGLE TRENDS & SEARCH INTENT INTEGRATION"));
        assertTrue(mediaPrompt.contains("Is Harry Kane starting today vs Dortmund"));
        assertTrue(mediaPrompt.contains("Bayern Munich lineup without Kane"));
        assertTrue(mediaPrompt.contains("ORGANIC SEARCH WEAVING INSTRUCTION:"));
        assertTrue(mediaPrompt.contains("ROSTER DELTA: «Bayern Munich»: Kane out with ankle sprain"));

        // Act 2: Build prompt for SMARTBET_QUANT in Spanish
        String quantPrompt = narrativeEngine.buildLlmEditorialPrompt(
                blueprint, trends, "es", Tone.SMARTBET_QUANT, "v1", null
        );

        // Assert quant prompt
        assertNotNull(quantPrompt);
        assertTrue(quantPrompt.contains("SmartBet Quant"));
        assertTrue(quantPrompt.contains("Spanish (es)"));
        assertTrue(quantPrompt.contains("GOOGLE TRENDS & SEARCH INTENT INTEGRATION"));
        assertTrue(quantPrompt.contains("Der Klassiker head to head record"));
    }

    @Test
    @DisplayName("MatchVisionReconciler accurately scores 45-parameter matrix and verifies >= 80% target")
    void testReconciliationHitRate() throws Exception {
        // Arrange
        List<TacticalMatrixPredictionDto> matrix = matchVisionService.generateTacticalMatrix(
                Sports.FOOTBALL,
                BigDecimal.valueOf(0.33),
                BigDecimal.valueOf(0.34),
                BigDecimal.valueOf(0.33),
                null, null
        );

        MatchVisionBlueprint entity = MatchVisionBlueprint.builder()
                .matchId(99999L)
                .sportName("FOOTBALL")
                .homeTeam("Team Alpha")
                .awayTeam("Team Beta")
                .tacticalMatrixJson(objectMapper.writeValueAsString(matrix))
                .status("PROJECTED")
                .build();

        when(blueprintRepository.findByMatchId(99999L)).thenReturn(Optional.of(entity));

        // Simulated SofaScore statistics JSON representing a tight 1:1 match across ALL, 1ST, 2ND
        String statsJson = """
        {
            "statistics": [
                {
                    "period": "ALL",
                    "groups": [
                        {
                            "groupName": "Match overview",
                            "statisticsItems": [
                                {"name": "Total shots", "home": "9", "away": "8"},
                                {"name": "Shots on target", "home": "4", "away": "3"},
                                {"name": "Expected goals", "home": "0.95", "away": "0.85"},
                                {"name": "Corner kicks", "home": "4", "away": "3"},
                                {"name": "Fouls", "home": "12", "away": "11"},
                                {"name": "Yellow cards", "home": "2", "away": "2"},
                                {"name": "Red cards", "home": "0", "away": "0"},
                                {"name": "Ball possession", "home": "51%", "away": "49%"},
                                {"name": "Goalkeeper saves", "home": "3", "away": "3"},
                                {"name": "Tackles", "home": "8", "away": "9"}
                            ]
                        },
                        {
                            "groupName": "Shots",
                            "statisticsItems": [
                                {"name": "Shots inside box", "home": "5", "away": "5"},
                                {"name": "Shots outside box", "home": "4", "away": "3"}
                            ]
                        },
                        {
                            "groupName": "Attack",
                            "statisticsItems": [
                                {"name": "Touches in penalty area", "home": "11", "away": "10"},
                                {"name": "Big chances", "home": "1", "away": "1"},
                                {"name": "Offsides", "home": "1", "away": "1"},
                                {"name": "Through balls", "home": "1", "away": "1"}
                            ]
                        },
                        {
                            "groupName": "Passes",
                            "statisticsItems": [
                                {"name": "Accurate passes", "home": "78%", "away": "76%"},
                                {"name": "Final third phase", "home": "68%", "away": "66%"}
                            ]
                        },
                        {
                            "groupName": "Duels",
                            "statisticsItems": [
                                {"name": "Aerial duels", "home": "52%", "away": "48%"},
                                {"name": "Ground duels", "home": "60", "away": "55"},
                                {"name": "Dribbles", "home": "55%", "away": "50%"},
                                {"name": "Dispossessed", "home": "5", "away": "6"}
                            ]
                        },
                        {
                            "groupName": "Defending",
                            "statisticsItems": [
                                {"name": "Interceptions", "home": "8", "away": "9"},
                                {"name": "Clearances", "home": "10", "away": "11"}
                            ]
                        },
                        {
                            "groupName": "Goalkeeping",
                            "statisticsItems": [
                                {"name": "High claims", "home": "1", "away": "1"}
                            ]
                        }
                    ]
                },
                {
                    "period": "1ST",
                    "groups": [
                        {
                            "groupName": "Match overview",
                            "statisticsItems": [
                                {"name": "Shots on target", "home": "2", "away": "1"},
                                {"name": "Corner kicks", "home": "2", "away": "1"},
                                {"name": "Yellow cards", "home": "0", "away": "1"}
                            ]
                        }
                    ]
                },
                {
                    "period": "2ND",
                    "groups": [
                        {
                            "groupName": "Match overview",
                            "statisticsItems": [
                                {"name": "Total shots", "home": "6", "away": "6"},
                                {"name": "Corner kicks", "home": "3", "away": "3"},
                                {"name": "Yellow cards", "home": "2", "away": "1"}
                            ]
                        }
                    ]
                }
            ]
        }
        """;

        // Act - score: 1:1 (0:0 at half time)
        boolean ok = reconciler.reconcileMatchVision(99999L, "1:1 (0:0)", statsJson);

        // Assert
        assertTrue(ok);
        assertEquals("SETTLED", entity.getStatus());
        assertNotNull(entity.getMatrixAccuracyPct());

        double acc = entity.getMatrixAccuracyPct().doubleValue();
        System.out.println("Verified 45-Matrix Accuracy in Test: " + acc + "%");
        assertTrue(acc >= 80.0, "Expected accuracy to exceed 80% benchmark, was: " + acc + "%");
    }
}
