package pro.datawiki.igaming.playerfaces.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.playerfaces.domain.AvatarSourceProvider;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;
import pro.datawiki.igaming.playerfaces.domain.TourType;
import pro.datawiki.igaming.playerfaces.repository.PlayerFaceRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Resolves player entity mappings in Master Data Management (MDM),
 * linking raw bookmaker / scraper names to canonical PlayerFace entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TennisMdmPlayerResolver {

    private final PlayerFaceRepository playerFaceRepository;

    /**
     * Resolves an MDM PlayerFace entity by name, searching normalized name, aliases,
     * or creating a new entry if unmapped.
     *
     * @param rawPlayerName raw or scraped athlete name
     * @param tour ATP or WTA
     * @return resolved or created PlayerFace entity
     */
    @Transactional
    public PlayerFace resolveOrCreatePlayer(String rawPlayerName, TourType tour) {
        if (rawPlayerName == null || rawPlayerName.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }

        String normalized = PlayerFace.normalizeName(rawPlayerName);

        // 1. Exact normalized lookup
        Optional<PlayerFace> existing = playerFaceRepository.findFirstByNormalizedName(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2. Search aliases
        List<PlayerFace> aliasMatches = playerFaceRepository.searchByNameOrAlias(normalized);
        if (!aliasMatches.isEmpty()) {
            return aliasMatches.get(0);
        }

        // 3. Fallback: Create and persist new MDM record
        log.info("[Tennis MDM] Creating new master record for athlete '{}' (normalized: '{}', tour: {})",
                rawPlayerName, normalized, tour);

        PlayerFace newPlayer = PlayerFace.builder()
                .playerName(rawPlayerName.trim())
                .normalizedName(normalized)
                .aliases(rawPlayerName.trim())
                .sport(SportType.TENNIS)
                .tour(tour != null ? tour : inferTourFromName(rawPlayerName))
                .countryCode("UNK")
                .avatarUrl("https://cdn.smartbet.guru/avatars/fallback-tennis.webp")
                .sourceProvider(AvatarSourceProvider.FALLBACK_CYBERPUNK)
                .webpOptimized(true)
                .lastHarvestedAt(LocalDateTime.now())
                .build();

        return playerFaceRepository.save(newPlayer);
    }

    private TourType inferTourFromName(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("swiatek") || lower.contains("sabalenka") || lower.contains("gauff") || lower.contains("rybakina")) {
            return TourType.WTA;
        }
        return TourType.ATP;
    }
}
