package pro.datawiki.igaming.playerfaces.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.dto.HarvestResultDto;
import pro.datawiki.igaming.playerfaces.dto.PlayerBatchRequest;
import pro.datawiki.igaming.playerfaces.dto.PlayerBatchResponse;
import pro.datawiki.igaming.playerfaces.dto.PlayerFaceDto;
import pro.datawiki.igaming.playerfaces.dto.TennisMatchCardDto;
import pro.datawiki.igaming.playerfaces.harvester.PlayerHeadshotHarvester;
import pro.datawiki.igaming.playerfaces.repository.PlayerFaceRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core manager coordinating player face resolution, harvester cascade pipelines,
 * batch lookups for odd scrapers, and auto-seeding top ATP/WTA/MMA athletes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerHeadshotManagerService {

    private final PlayerFaceRepository playerFaceRepository;
    private final PlayerAvatarCdnService avatarCdnService;
    private final List<PlayerHeadshotHarvester> harvesters;

    private List<PlayerHeadshotHarvester> sortedHarvesters;

    @PostConstruct
    public void init() {
        this.sortedHarvesters = new ArrayList<>(harvesters);
        this.sortedHarvesters.sort(Comparator.comparingInt(PlayerHeadshotHarvester::getOrder));
        log.info("Initialized PlayerHeadshotManagerService with {} harvesters: {}",
                sortedHarvesters.size(),
                sortedHarvesters.stream().map(h -> h.getProvider().name()).toList());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            long count = playerFaceRepository.count();
            if (count < 50) {
                log.info("Current player_face catalog contains {} records (< 50). Starting top-50 catalog auto-seed...", count);
                seedTopAthletesCatalog();
            }
        } catch (Exception e) {
            log.warn("Auto-seeding catalog deferred (DB connection or table initialization): {}", e.getMessage());
        }
    }

    /**
     * Resolves player face profile. Returns existing record or triggers harvesting cascade.
     */
    @Transactional
    public PlayerFaceDto getOrHarvest(String playerName, SportType sport, TourType tour) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }

        String normalized = PlayerFace.normalizeName(playerName);
        Optional<PlayerFace> existing = playerFaceRepository.findFirstByNormalizedName(normalized);
        if (existing.isPresent()) {
            return PlayerFaceDto.fromEntity(existing.get());
        }

        // Search by alias
        List<PlayerFace> aliasMatches = playerFaceRepository.searchByNameOrAlias(normalized);
        if (!aliasMatches.isEmpty()) {
            return PlayerFaceDto.fromEntity(aliasMatches.get(0));
        }

        // Harvester cascade execution
        SportType effectiveSport = sport != null ? sport : SportType.TENNIS;
        for (PlayerHeadshotHarvester harvester : sortedHarvesters) {
            if (!harvester.isAvailable()) {
                continue;
            }
            try {
                Optional<PlayerFaceDto> harvested = harvester.harvest(playerName, effectiveSport, tour);
                if (harvested.isPresent()) {
                    PlayerFace entity = harvested.get().toEntity();
                    entity.setNormalizedName(normalized);
                    entity.setPlayerName(playerName.trim());
                    entity.setSport(effectiveSport);
                    if (tour != null) {
                        entity.setTour(tour);
                    }
                    entity.setLastHarvestedAt(LocalDateTime.now());
                    PlayerFace saved = playerFaceRepository.save(entity);
                    log.info("Harvested player portrait for '{}' via {}", playerName, harvester.getProvider());
                    return PlayerFaceDto.fromEntity(saved);
                }
            } catch (Exception e) {
                log.warn("Harvester {} failed for player '{}': {}", harvester.getProvider(), playerName, e.getMessage());
            }
        }

        // Fallback: Generate Cyberpunk SVG Avatar and cache in DB
        String fallbackCdnUrl = avatarCdnService.buildCdnUrl(playerName);
        PlayerFace fallback = PlayerFace.builder()
                .playerName(playerName.trim())
                .normalizedName(normalized)
                .sport(effectiveSport)
                .tour(tour)
                .avatarUrl(fallbackCdnUrl)
                .sourceUrl(fallbackCdnUrl)
                .sourceProvider(AvatarSourceProvider.FALLBACK_CYBERPUNK)
                .webpOptimized(Boolean.TRUE)
                .lastHarvestedAt(LocalDateTime.now())
                .build();

        PlayerFace saved = playerFaceRepository.save(fallback);
        log.info("Generated fallback Cyberpunk avatar for '{}' -> {}", playerName, fallbackCdnUrl);
        return PlayerFaceDto.fromEntity(saved);
    }

    /**
     * Explicitly forces an external harvesting attempt with execution timing.
     */
    @Transactional
    public HarvestResultDto forceHarvest(String playerName, SportType sport, TourType tour) {
        long startTime = System.currentTimeMillis();
        if (playerName == null || playerName.isBlank()) {
            return HarvestResultDto.builder()
                    .playerName(playerName)
                    .success(false)
                    .message("Player name cannot be blank")
                    .durationMs(0)
                    .build();
        }

        SportType effectiveSport = sport != null ? sport : SportType.TENNIS;
        for (PlayerHeadshotHarvester harvester : sortedHarvesters) {
            try {
                Optional<PlayerFaceDto> harvested = harvester.harvest(playerName, effectiveSport, tour);
                if (harvested.isPresent()) {
                    PlayerFace entity = harvested.get().toEntity();
                    entity.setNormalizedName(PlayerFace.normalizeName(playerName));
                    entity.setPlayerName(playerName.trim());
                    entity.setSport(effectiveSport);
                    entity.setLastHarvestedAt(LocalDateTime.now());
                    PlayerFace saved = playerFaceRepository.save(entity);

                    long duration = System.currentTimeMillis() - startTime;
                    return HarvestResultDto.builder()
                            .playerName(playerName)
                            .success(true)
                            .avatarUrl(saved.getAvatarUrl())
                            .provider(harvester.getProvider())
                            .message("Successfully harvested via " + harvester.getProvider().getDescription())
                            .durationMs(duration)
                            .build();
                }
            } catch (Exception e) {
                log.debug("Force harvest attempt via {} failed: {}", harvester.getProvider(), e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        return HarvestResultDto.builder()
                .playerName(playerName)
                .success(false)
                .message("No portraits found across external providers. Fallback avatar remains active.")
                .durationMs(duration)
                .build();
    }

    /**
     * Resolves batch of player names for odd ingestion and line crawlers.
     */
    @Transactional
    public PlayerBatchResponse resolveBatch(PlayerBatchRequest request) {
        if (request == null || request.getPlayerNames() == null) {
            return PlayerBatchResponse.builder()
                    .players(Map.of())
                    .totalRequested(0)
                    .totalResolved(0)
                    .totalGenerated(0)
                    .build();
        }

        Map<String, PlayerFaceDto> resolvedMap = new HashMap<>();
        int generatedCount = 0;

        for (String rawName : request.getPlayerNames()) {
            if (rawName == null || rawName.isBlank()) {
                continue;
            }
            PlayerFaceDto dto = getOrHarvest(rawName, request.getSport(), request.getTour());
            if (dto != null) {
                resolvedMap.put(rawName, dto);
                if (dto.getSourceProvider() == AvatarSourceProvider.FALLBACK_CYBERPUNK) {
                    generatedCount++;
                }
            }
        }

        return PlayerBatchResponse.builder()
                .players(resolvedMap)
                .totalRequested(request.getPlayerNames().size())
                .totalResolved(resolvedMap.size())
                .totalGenerated(generatedCount)
                .build();
    }

    /**
     * Retrieves top ranked athletes for given sport.
     */
    public List<PlayerFaceDto> getTopPlayers(SportType sport) {
        SportType eff = sport != null ? sport : SportType.TENNIS;
        return playerFaceRepository.findTop50BySportOrderByRankingAsc(eff).stream()
                .map(PlayerFaceDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Enriches Head-to-Head tennis or solo sport match card with portraits,
     * seed rankings, national flags and surebet profit calculations.
     */
    @Transactional
    public TennisMatchCardDto enrichTennisMatchCard(TennisMatchCardDto card) {
        if (card == null) {
            return null;
        }

        SportType sport = card.getSport() != null ? card.getSport() : SportType.TENNIS;
        TourType tour = card.getTour() != null ? card.getTour() : TourType.ATP;

        if (card.getPlayer1() != null && card.getPlayer1().getPlayerName() != null) {
            PlayerFaceDto enriched1 = getOrHarvest(card.getPlayer1().getPlayerName(), sport, tour);
            if (enriched1 != null) {
                // Keep caller's seed number if passed
                if (card.getPlayer1().getSeedNumber() != null) {
                    enriched1.setSeedNumber(card.getPlayer1().getSeedNumber());
                }
                card.setPlayer1(enriched1);
            }
        }

        if (card.getPlayer2() != null && card.getPlayer2().getPlayerName() != null) {
            PlayerFaceDto enriched2 = getOrHarvest(card.getPlayer2().getPlayerName(), sport, tour);
            if (enriched2 != null) {
                if (card.getPlayer2().getSeedNumber() != null) {
                    enriched2.setSeedNumber(card.getPlayer2().getSeedNumber());
                }
                card.setPlayer2(enriched2);
            }
        }

        // Calculate Surebet Arbitrage Profit Percentage if both odds present
        if (card.getOdds1() != null && card.getOdds1() > 1.0 && card.getOdds2() != null && card.getOdds2() > 1.0) {
            double margin = (1.0 / card.getOdds1()) + (1.0 / card.getOdds2());
            double profitPercent = (1.0 - margin) * 100.0;
            // Round to 2 decimal places
            card.setSurebetProfitPercent(Math.round(profitPercent * 100.0) / 100.0);
        }

        return card;
    }

    /**
     * Seeds the catalog with Top 50 international athletes across ATP, WTA, and MMA/UFC.
     */
    @Transactional
    public int seedTopAthletesCatalog() {
        List<PlayerFace> topCatalog = new ArrayList<>();

        // === ATP TOP 20 ===
        topCatalog.add(createSeedAthlete("Novak Djokovic", SportType.TENNIS, TourType.ATP, "Serbia", "SRB", "https://flagcdn.com/w80/rs.png", 1, 1, "https://www.thesportsdb.com/images/media/player/cutout/e6b2fe1594917610.png", "Nole, Djoker"));
        topCatalog.add(createSeedAthlete("Carlos Alcaraz", SportType.TENNIS, TourType.ATP, "Spain", "ESP", "https://flagcdn.com/w80/es.png", 2, 2, "https://www.thesportsdb.com/images/media/player/cutout/0b968c1689531189.png", "Carlitos"));
        topCatalog.add(createSeedAthlete("Jannik Sinner", SportType.TENNIS, TourType.ATP, "Italy", "ITA", "https://flagcdn.com/w80/it.png", 3, 3, "https://www.thesportsdb.com/images/media/player/cutout/2b92ab1689531388.png", "Fox"));
        topCatalog.add(createSeedAthlete("Daniil Medvedev", SportType.TENNIS, TourType.ATP, "Russia", "RUS", "https://flagcdn.com/w80/ru.png", 4, 4, "https://www.thesportsdb.com/images/media/player/cutout/b1154f1689531278.png", "Bear"));
        topCatalog.add(createSeedAthlete("Alexander Zverev", SportType.TENNIS, TourType.ATP, "Germany", "GER", "https://flagcdn.com/w80/de.png", 5, 5, "https://www.thesportsdb.com/images/media/player/cutout/405a8b1689531333.png", "Sascha"));
        topCatalog.add(createSeedAthlete("Andrey Rublev", SportType.TENNIS, TourType.ATP, "Russia", "RUS", "https://flagcdn.com/w80/ru.png", 6, 6, "https://www.thesportsdb.com/images/media/player/cutout/4c022f1689531422.png", "Rublo"));
        topCatalog.add(createSeedAthlete("Stefanos Tsitsipas", SportType.TENNIS, TourType.ATP, "Greece", "GRE", "https://flagcdn.com/w80/gr.png", 7, 7, "https://www.thesportsdb.com/images/media/player/cutout/981c2f1689531455.png", "Tsitsi"));
        topCatalog.add(createSeedAthlete("Casper Ruud", SportType.TENNIS, TourType.ATP, "Norway", "NOR", "https://flagcdn.com/w80/no.png", 8, 8, "https://www.thesportsdb.com/images/media/player/cutout/1bc8e91689531502.png", "Ruud"));
        topCatalog.add(createSeedAthlete("Hubert Hurkacz", SportType.TENNIS, TourType.ATP, "Poland", "POL", "https://flagcdn.com/w80/pl.png", 9, 9, "https://www.thesportsdb.com/images/media/player/cutout/2ba54d1689531535.png", "Hubi"));
        topCatalog.add(createSeedAthlete("Alex de Minaur", SportType.TENNIS, TourType.ATP, "Australia", "AUS", "https://flagcdn.com/w80/au.png", 10, 10, "https://www.thesportsdb.com/images/media/player/cutout/9de77e1689531568.png", "Demon"));
        topCatalog.add(createSeedAthlete("Grigor Dimitrov", SportType.TENNIS, TourType.ATP, "Bulgaria", "BUL", "https://flagcdn.com/w80/bg.png", 11, 11, "https://www.thesportsdb.com/images/media/player/cutout/1d34f41689531601.png", "Grisha"));
        topCatalog.add(createSeedAthlete("Taylor Fritz", SportType.TENNIS, TourType.ATP, "United States", "USA", "https://flagcdn.com/w80/us.png", 12, 12, "https://www.thesportsdb.com/images/media/player/cutout/72e6b21689531634.png", "Fritz"));
        topCatalog.add(createSeedAthlete("Holger Rune", SportType.TENNIS, TourType.ATP, "Denmark", "DEN", "https://flagcdn.com/w80/dk.png", 13, 13, "https://www.thesportsdb.com/images/media/player/cutout/3fa91c1689531670.png", "Rune"));
        topCatalog.add(createSeedAthlete("Tommy Paul", SportType.TENNIS, TourType.ATP, "United States", "USA", "https://flagcdn.com/w80/us.png", 14, 14, "https://www.thesportsdb.com/images/media/player/cutout/4eb25e1689531705.png", "TP"));
        topCatalog.add(createSeedAthlete("Ben Shelton", SportType.TENNIS, TourType.ATP, "United States", "USA", "https://flagcdn.com/w80/us.png", 15, 15, "https://www.thesportsdb.com/images/media/player/cutout/5da78a1689531742.png", "Shelton"));
        topCatalog.add(createSeedAthlete("Frances Tiafoe", SportType.TENNIS, TourType.ATP, "United States", "USA", "https://flagcdn.com/w80/us.png", 16, 16, "https://www.thesportsdb.com/images/media/player/cutout/6eb41a1689531780.png", "Big Foe"));
        topCatalog.add(createSeedAthlete("Ugo Humbert", SportType.TENNIS, TourType.ATP, "France", "FRA", "https://flagcdn.com/w80/fr.png", 17, 17, "https://www.thesportsdb.com/images/media/player/cutout/7fb93c1689531818.png", "Humbert"));
        topCatalog.add(createSeedAthlete("Sebastian Baez", SportType.TENNIS, TourType.ATP, "Argentina", "ARG", "https://flagcdn.com/w80/ar.png", 18, 18, "https://www.thesportsdb.com/images/media/player/cutout/8ec21b1689531855.png", "Seba"));
        topCatalog.add(createSeedAthlete("Alexander Bublik", SportType.TENNIS, TourType.ATP, "Kazakhstan", "KAZ", "https://flagcdn.com/w80/kz.png", 19, 19, "https://www.thesportsdb.com/images/media/player/cutout/9fa32c1689531890.png", "Bublik, Sasha"));
        topCatalog.add(createSeedAthlete("Karen Khachanov", SportType.TENNIS, TourType.ATP, "Russia", "RUS", "https://flagcdn.com/w80/ru.png", 20, 20, "https://www.thesportsdb.com/images/media/player/cutout/0ab43d1689531925.png", "Djan"));

        // === WTA TOP 15 ===
        topCatalog.add(createSeedAthlete("Iga Swiatek", SportType.TENNIS, TourType.WTA, "Poland", "POL", "https://flagcdn.com/w80/pl.png", 1, 1, "https://www.thesportsdb.com/images/media/player/cutout/3eb91a1689532010.png", "1ga"));
        topCatalog.add(createSeedAthlete("Aryna Sabalenka", SportType.TENNIS, TourType.WTA, "Belarus", "BLR", "https://flagcdn.com/w80/by.png", 2, 2, "https://www.thesportsdb.com/images/media/player/cutout/4fb02b1689532045.png", "Tiger"));
        topCatalog.add(createSeedAthlete("Coco Gauff", SportType.TENNIS, TourType.WTA, "United States", "USA", "https://flagcdn.com/w80/us.png", 3, 3, "https://www.thesportsdb.com/images/media/player/cutout/5ab13c1689532080.png", "Cori"));
        topCatalog.add(createSeedAthlete("Elena Rybakina", SportType.TENNIS, TourType.WTA, "Kazakhstan", "KAZ", "https://flagcdn.com/w80/kz.png", 4, 4, "https://www.thesportsdb.com/images/media/player/cutout/6bc24d1689532115.png", "Ice Queen"));
        topCatalog.add(createSeedAthlete("Jessica Pegula", SportType.TENNIS, TourType.WTA, "United States", "USA", "https://flagcdn.com/w80/us.png", 5, 5, "https://www.thesportsdb.com/images/media/player/cutout/7cd35e1689532150.png", "Jessie"));
        topCatalog.add(createSeedAthlete("Marketa Vondrousova", SportType.TENNIS, TourType.WTA, "Czech Republic", "CZE", "https://flagcdn.com/w80/cz.png", 6, 6, "https://www.thesportsdb.com/images/media/player/cutout/8de46f1689532185.png", "Maky"));
        topCatalog.add(createSeedAthlete("Ons Jabeur", SportType.TENNIS, TourType.WTA, "Tunisia", "TUN", "https://flagcdn.com/w80/tn.png", 7, 7, "https://www.thesportsdb.com/images/media/player/cutout/9ef57a1689532220.png", "Minister of Happiness"));
        topCatalog.add(createSeedAthlete("Maria Sakkari", SportType.TENNIS, TourType.WTA, "Greece", "GRE", "https://flagcdn.com/w80/gr.png", 8, 8, "https://www.thesportsdb.com/images/media/player/cutout/0fa68b1689532255.png", "Sakk"));
        topCatalog.add(createSeedAthlete("Qinwen Zheng", SportType.TENNIS, TourType.WTA, "China", "CHN", "https://flagcdn.com/w80/cn.png", 9, 9, "https://www.thesportsdb.com/images/media/player/cutout/1ab79c1689532290.png", "Queenwen"));
        topCatalog.add(createSeedAthlete("Jelena Ostapenko", SportType.TENNIS, TourType.WTA, "Latvia", "LAT", "https://flagcdn.com/w80/lv.png", 10, 10, "https://www.thesportsdb.com/images/media/player/cutout/2bc80d1689532325.png", "Penko"));
        topCatalog.add(createSeedAthlete("Daria Kasatkina", SportType.TENNIS, TourType.WTA, "Russia", "RUS", "https://flagcdn.com/w80/ru.png", 11, 11, "https://www.thesportsdb.com/images/media/player/cutout/3cd91e1689532360.png", "Dasha"));
        topCatalog.add(createSeedAthlete("Danielle Collins", SportType.TENNIS, TourType.WTA, "United States", "USA", "https://flagcdn.com/w80/us.png", 12, 12, "https://www.thesportsdb.com/images/media/player/cutout/4de02f1689532395.png", "Danimal"));
        topCatalog.add(createSeedAthlete("Madison Keys", SportType.TENNIS, TourType.WTA, "United States", "USA", "https://flagcdn.com/w80/us.png", 13, 13, "https://www.thesportsdb.com/images/media/player/cutout/5ef13a1689532430.png", "Madi"));
        topCatalog.add(createSeedAthlete("Mirra Andreeva", SportType.TENNIS, TourType.WTA, "Russia", "RUS", "https://flagcdn.com/w80/ru.png", 14, 14, "https://www.thesportsdb.com/images/media/player/cutout/6fa24b1689532465.png", "Mirra"));
        topCatalog.add(createSeedAthlete("Victoria Azarenka", SportType.TENNIS, TourType.WTA, "Belarus", "BLR", "https://flagcdn.com/w80/by.png", 15, 15, "https://www.thesportsdb.com/images/media/player/cutout/7ab35c1689532500.png", "Vika"));

        // === MMA / UFC TOP 15 ===
        topCatalog.add(createSeedAthlete("Islam Makhachev", SportType.MMA, TourType.UFC, "Russia", "RUS", "https://flagcdn.com/w80/ru.png", 1, 1, "https://www.thesportsdb.com/images/media/player/cutout/8bc46d1689532600.png", "P4P King"));
        topCatalog.add(createSeedAthlete("Jon Jones", SportType.MMA, TourType.UFC, "United States", "USA", "https://flagcdn.com/w80/us.png", 2, 2, "https://www.thesportsdb.com/images/media/player/cutout/9cd57e1689532635.png", "Bones"));
        topCatalog.add(createSeedAthlete("Alex Pereira", SportType.MMA, TourType.UFC, "Brazil", "BRA", "https://flagcdn.com/w80/br.png", 3, 3, "https://www.thesportsdb.com/images/media/player/cutout/0de68f1689532670.png", "Poatan"));
        topCatalog.add(createSeedAthlete("Leon Edwards", SportType.MMA, TourType.UFC, "United Kingdom", "GBR", "https://flagcdn.com/w80/gb.png", 4, 4, "https://www.thesportsdb.com/images/media/player/cutout/1ef79a1689532705.png", "Rocky"));
        topCatalog.add(createSeedAthlete("Sean O'Malley", SportType.MMA, TourType.UFC, "United States", "USA", "https://flagcdn.com/w80/us.png", 5, 5, "https://www.thesportsdb.com/images/media/player/cutout/2fa80b1689532740.png", "Suga"));
        topCatalog.add(createSeedAthlete("Ilia Topuria", SportType.MMA, TourType.UFC, "Spain", "ESP", "https://flagcdn.com/w80/es.png", 6, 6, "https://www.thesportsdb.com/images/media/player/cutout/3ab91c1689532775.png", "El Matador"));
        topCatalog.add(createSeedAthlete("Dricus du Plessis", SportType.MMA, TourType.UFC, "South Africa", "ZAF", "https://flagcdn.com/w80/za.png", 7, 7, "https://www.thesportsdb.com/images/media/player/cutout/4bc02d1689532810.png", "Stillknocks"));
        topCatalog.add(createSeedAthlete("Alexandre Pantoja", SportType.MMA, TourType.UFC, "Brazil", "BRA", "https://flagcdn.com/w80/br.png", 8, 8, "https://www.thesportsdb.com/images/media/player/cutout/5cd13e1689532845.png", "The Cannibal"));
        topCatalog.add(createSeedAthlete("Max Holloway", SportType.MMA, TourType.UFC, "United States", "USA", "https://flagcdn.com/w80/us.png", 9, 9, "https://www.thesportsdb.com/images/media/player/cutout/6de24f1689532880.png", "Blessed"));
        topCatalog.add(createSeedAthlete("Charles Oliveira", SportType.MMA, TourType.UFC, "Brazil", "BRA", "https://flagcdn.com/w80/br.png", 10, 10, "https://www.thesportsdb.com/images/media/player/cutout/7ef35a1689532915.png", "Do Bronx"));
        topCatalog.add(createSeedAthlete("Arman Tsarukyan", SportType.MMA, TourType.UFC, "Armenia", "ARM", "https://flagcdn.com/w80/am.png", 11, 11, "https://www.thesportsdb.com/images/media/player/cutout/8fa46b1689532950.png", "Ahalkalakets"));
        topCatalog.add(createSeedAthlete("Khamzat Chimaev", SportType.MMA, TourType.UFC, "United Arab Emirates", "ARE", "https://flagcdn.com/w80/ae.png", 12, 12, "https://www.thesportsdb.com/images/media/player/cutout/9ab57c1689532985.png", "Borz"));
        topCatalog.add(createSeedAthlete("Justin Gaethje", SportType.MMA, TourType.UFC, "United States", "USA", "https://flagcdn.com/w80/us.png", 13, 13, "https://www.thesportsdb.com/images/media/player/cutout/0bc68d1689533020.png", "The Highlight"));
        topCatalog.add(createSeedAthlete("Dustin Poirier", SportType.MMA, TourType.UFC, "United States", "USA", "https://flagcdn.com/w80/us.png", 14, 14, "https://www.thesportsdb.com/images/media/player/cutout/1cd79e1689533055.png", "The Diamond"));
        topCatalog.add(createSeedAthlete("Tom Aspinall", SportType.MMA, TourType.UFC, "United Kingdom", "GBR", "https://flagcdn.com/w80/gb.png", 15, 15, "https://www.thesportsdb.com/images/media/player/cutout/2de80f1689533090.png", "Aspinall"));

        int savedCount = 0;
        for (PlayerFace p : topCatalog) {
            if (!playerFaceRepository.existsByNormalizedName(p.getNormalizedName())) {
                playerFaceRepository.save(p);
                savedCount++;
            }
        }

        log.info("Catalog seeding finished: successfully seeded {} top athletes (ATP, WTA, UFC/MMA).", savedCount);
        return savedCount;
    }

    private PlayerFace createSeedAthlete(String name, SportType sport, TourType tour, String country,
                                         String countryCode, String flagUrl, int ranking, int seed,
                                         String avatarUrl, String aliases) {
        return PlayerFace.builder()
                .playerName(name)
                .normalizedName(PlayerFace.normalizeName(name))
                .aliases(aliases)
                .sport(sport)
                .tour(tour)
                .country(country)
                .countryCode(countryCode)
                .flagUrl(flagUrl)
                .ranking(ranking)
                .seedNumber(seed)
                .avatarUrl(avatarUrl != null ? avatarUrl : avatarCdnService.buildCdnUrl(name))
                .sourceUrl(avatarUrl)
                .sourceProvider(AvatarSourceProvider.THE_SPORTS_DB)
                .webpOptimized(Boolean.TRUE)
                .lastHarvestedAt(LocalDateTime.now())
                .build();
    }
}
