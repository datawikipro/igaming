package pro.datawiki.igaming.playerfaces.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.HarvestResultDto;
import pro.datawiki.igaming.playerfaces.dto.PlayerBatchRequest;
import pro.datawiki.igaming.playerfaces.dto.PlayerBatchResponse;
import pro.datawiki.igaming.playerfaces.dto.PlayerFaceDto;
import pro.datawiki.igaming.playerfaces.dto.TennisMatchCardDto;
import pro.datawiki.igaming.playerfaces.repository.PlayerFaceRepository;
import pro.datawiki.igaming.playerfaces.service.PlayerHeadshotManagerService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API controller exposing player face avatar profiles, search,
 * batch resolution for crawlers, and solo match card enrichment.
 */
@Slf4j
@RestController
@RequestMapping({"/api/players/faces", "/api/v1/players"})
@RequiredArgsConstructor
public class PlayerFaceController {

    private final PlayerHeadshotManagerService headshotManagerService;
    private final PlayerFaceRepository playerFaceRepository;

    /**
     * Lists or filters player faces by sport, tour, or search text.
     */
    @GetMapping
    public ResponseEntity<List<PlayerFaceDto>> listPlayerFaces(
            @RequestParam(required = false) SportType sport,
            @RequestParam(required = false) TourType tour,
            @RequestParam(required = false) String search) {

        List<PlayerFace> results;
        if (search != null && !search.isBlank()) {
            results = playerFaceRepository.searchByNameOrAlias(search.trim());
        } else if (sport != null && tour != null) {
            results = playerFaceRepository.findBySportAndTour(sport, tour);
        } else if (sport != null) {
            results = playerFaceRepository.findBySport(sport);
        } else {
            results = playerFaceRepository.findAll();
        }

        List<PlayerFaceDto> dtos = results.stream()
                .map(PlayerFaceDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Finds existing athlete or triggers automated harvester cascade by player name.
     */
    @GetMapping("/by-name")
    public ResponseEntity<PlayerFaceDto> getByName(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "TENNIS") SportType sport,
            @RequestParam(required = false) TourType tour) {

        PlayerFaceDto player = headshotManagerService.getOrHarvest(name, sport, tour);
        if (player == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(player);
    }

    /**
     * Batch resolution endpoint for odd scrapers and surebet matchers.
     */
    @PostMapping("/batch")
    public ResponseEntity<PlayerBatchResponse> batchResolve(@RequestBody PlayerBatchRequest request) {
        PlayerBatchResponse response = headshotManagerService.resolveBatch(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Triggers explicit re-harvesting attempt from external sources.
     */
    @PostMapping("/harvest")
    public ResponseEntity<HarvestResultDto> harvestPlayer(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "TENNIS") SportType sport,
            @RequestParam(required = false) TourType tour) {

        HarvestResultDto result = headshotManagerService.forceHarvest(name, sport, tour);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves top-50 ranked athletes for a given sport.
     */
    @GetMapping("/top")
    public ResponseEntity<List<PlayerFaceDto>> getTopPlayers(
            @RequestParam(required = false, defaultValue = "TENNIS") SportType sport) {

        List<PlayerFaceDto> top = headshotManagerService.getTopPlayers(sport);
        return ResponseEntity.ok(top);
    }

    /**
     * Enriches solo match card with player avatars, flags, seeds, rankings, and surebet profit.
     */
    @PostMapping("/card")
    public ResponseEntity<TennisMatchCardDto> enrichCard(@RequestBody TennisMatchCardDto card) {
        TennisMatchCardDto enriched = headshotManagerService.enrichTennisMatchCard(card);
        return ResponseEntity.ok(enriched);
    }

    /**
     * Generates a sample Head-to-Head tennis card for demonstration or testing.
     */
    @GetMapping("/card")
    public ResponseEntity<TennisMatchCardDto> getSampleTennisCard(
            @RequestParam(defaultValue = "Novak Djokovic") String p1,
            @RequestParam(defaultValue = "Carlos Alcaraz") String p2,
            @RequestParam(defaultValue = "1.92") Double odds1,
            @RequestParam(defaultValue = "2.14") Double odds2) {

        TennisMatchCardDto sample = TennisMatchCardDto.builder()
                .matchId("demo-match-101")
                .tournamentName("Wimbledon - Men's Singles Final")
                .round("Final")
                .surface("Grass")
                .sport(SportType.TENNIS)
                .tour(TourType.ATP)
                .player1(PlayerFaceDto.builder().playerName(p1).seedNumber(1).build())
                .player2(PlayerFaceDto.builder().playerName(p2).seedNumber(2).build())
                .odds1(odds1)
                .odds2(odds2)
                .currentScore("6-4, 4-6, 3-2 (30-15*)")
                .status("LIVE")
                .build();

        TennisMatchCardDto enriched = headshotManagerService.enrichTennisMatchCard(sample);
        return ResponseEntity.ok(enriched);
    }

    /**
     * Manually triggers seeding of the top-50 catalog.
     */
    @PostMapping("/seed-catalog")
    public ResponseEntity<String> seedCatalog() {
        int seeded = headshotManagerService.seedTopAthletesCatalog();
        return ResponseEntity.ok("Seeded " + seeded + " athletes into catalog.");
    }
}
