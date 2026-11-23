package pro.datawiki.igaming.mdm.service.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.mdm.domain.MdmPlayer;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTeamMembership;
import pro.datawiki.igaming.mdm.repository.MdmPlayerRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamMembershipRepository;
import pro.datawiki.igaming.mdm.repository.MdmTeamRepository;
import pro.datawiki.igaming.mdm.service.generator.AthleteAvatarGenerator;
import pro.datawiki.igaming.mdm.service.generator.TeamLogoGenerator;
import pro.datawiki.igaming.mdm.service.media.VisualAssetStorageService.MediaAsset;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the 3-tier waterfall resolution for team logos and athlete photos:
 *   Tier 1: SofaScore API (Official)
 *   Tier 2: TheSportsDB / Web Search (External Search)
 *   Tier 3: Procedural Vector SVG Generator (Generated)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VisualAssetResolverService {

    private static final String SOFASCORE_TEAM_IMAGE_URL = "https://api.sofascore.app/api/v1/team/%d/image";
    private static final String SOFASCORE_PLAYER_IMAGE_URL = "https://api.sofascore.app/api/v1/player/%d/image";

    private static final String TSDB_TEAM_SEARCH = "https://www.thesportsdb.com/api/v1/json/3/searchteams.php?t=";
    private static final String TSDB_PLAYER_SEARCH = "https://www.thesportsdb.com/api/v1/json/3/searchplayers.php?p=";

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 4000;
    private static final int MAX_IMAGE_BYTES = 4 * 1024 * 1024; // 4 MB limit

    private final VisualAssetStorageService storageService;
    private final TeamLogoGenerator teamLogoGenerator;
    private final AthleteAvatarGenerator athleteAvatarGenerator;

    private final MdmTeamRepository teamRepository;
    private final MdmPlayerRepository playerRepository;
    private final MdmTeamMembershipRepository membershipRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Resolves team logo through the 3-tier waterfall with local SSD caching.
     */
    @Transactional
    public MediaAsset resolveTeamLogo(Long teamId) {
        // 1. Check local disk cache first
        Optional<MediaAsset> cached = storageService.findAsset("teams", teamId);
        if (cached.isPresent()) {
            return cached.get();
        }

        Optional<MdmTeam> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            // Unregistered team fallback
            String svg = teamLogoGenerator.generateSvgCrest("Team " + teamId, "FOOTBALL");
            return new MediaAsset(svg.getBytes(StandardCharsets.UTF_8), "image/svg+xml", "svg");
        }

        MdmTeam team = teamOpt.get();
        String sportSlug = team.getSport() != null ? team.getSport().getSlug() : "FOOTBALL";

        // Tier 1: SofaScore Official Fetch
        if (team.getSofascoreId() != null && team.getSofascoreId() > 0) {
            String sofaUrl = SOFASCORE_TEAM_IMAGE_URL.formatted(team.getSofascoreId());
            byte[] bytes = fetchImageBytes(sofaUrl);
            if (bytes != null && bytes.length > 500) { // Valid image payload
                storageService.saveAsset("teams", teamId, "png", bytes);
                team.setLogoUrl("/api/v1/mdm/media/teams/" + teamId + "/logo");
                team.setLogoSource("OFFICIAL");
                team.setIsOfficialLogo(true);
                teamRepository.save(team);
                log.info("Team logo resolved via SofaScore (OFFICIAL) for teamId={}: '{}'", teamId, team.getCanonicalName());
                return new MediaAsset(bytes, "image/png", "png");
            }
        }

        // Tier 2: TheSportsDB External Search
        String tsdbLogoUrl = searchTheSportsDbTeamBadge(team.getCanonicalName());
        if (tsdbLogoUrl != null) {
            byte[] bytes = fetchImageBytes(tsdbLogoUrl);
            if (bytes != null && bytes.length > 500) {
                storageService.saveAsset("teams", teamId, "png", bytes);
                team.setLogoUrl("/api/v1/mdm/media/teams/" + teamId + "/logo");
                team.setLogoSource("EXTERNAL_SEARCH");
                team.setIsOfficialLogo(false);
                teamRepository.save(team);
                log.info("Team logo resolved via TheSportsDB (EXTERNAL_SEARCH) for teamId={}: '{}'", teamId, team.getCanonicalName());
                return new MediaAsset(bytes, "image/png", "png");
            }
        }

        // Tier 3: Procedural Vector SVG Generator
        String svg = teamLogoGenerator.generateSvgCrest(team.getCanonicalName(), sportSlug);
        byte[] svgBytes = svg.getBytes(StandardCharsets.UTF_8);
        storageService.saveAsset("teams", teamId, "svg", svgBytes);

        team.setLogoUrl("/api/v1/mdm/media/teams/" + teamId + "/logo");
        team.setLogoSource("GENERATED");
        team.setIsOfficialLogo(false);
        teamRepository.save(team);
        log.info("Team logo generated procedurally (GENERATED) for teamId={}: '{}'", teamId, team.getCanonicalName());

        return new MediaAsset(svgBytes, "image/svg+xml", "svg");
    }

    /**
     * Resolves athlete photo through the 3-tier waterfall with local SSD caching.
     */
    @Transactional
    public MediaAsset resolvePlayerPhoto(Long playerId) {
        // 1. Check local disk cache first
        Optional<MediaAsset> cached = storageService.findAsset("players", playerId);
        if (cached.isPresent()) {
            return cached.get();
        }

        Optional<MdmPlayer> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            String svg = athleteAvatarGenerator.generateSvgAvatar("Player " + playerId, null, "FOOTBALL", null);
            return new MediaAsset(svg.getBytes(StandardCharsets.UTF_8), "image/svg+xml", "svg");
        }

        MdmPlayer player = playerOpt.get();
        String sportSlug = player.getSport() != null ? player.getSport().getSlug() : "FOOTBALL";

        // Tier 1: SofaScore Official Fetch
        if (player.getSofascoreId() != null && player.getSofascoreId() > 0) {
            String sofaUrl = SOFASCORE_PLAYER_IMAGE_URL.formatted(player.getSofascoreId());
            byte[] bytes = fetchImageBytes(sofaUrl);
            if (bytes != null && bytes.length > 500) {
                storageService.saveAsset("players", playerId, "png", bytes);
                player.setPhotoUrl("/api/v1/mdm/media/players/" + playerId + "/photo");
                player.setPhotoSource("OFFICIAL");
                player.setIsOfficialPhoto(true);
                playerRepository.save(player);
                log.info("Athlete photo resolved via SofaScore (OFFICIAL) for playerId={}: '{}'", playerId, player.getCommonName());
                return new MediaAsset(bytes, "image/png", "png");
            }
        }

        // Tier 2: TheSportsDB External Search
        String tsdbPlayerPhotoUrl = searchTheSportsDbPlayerPhoto(player.getCommonName());
        if (tsdbPlayerPhotoUrl != null) {
            byte[] bytes = fetchImageBytes(tsdbPlayerPhotoUrl);
            if (bytes != null && bytes.length > 500) {
                storageService.saveAsset("players", playerId, "png", bytes);
                player.setPhotoUrl("/api/v1/mdm/media/players/" + playerId + "/photo");
                player.setPhotoSource("EXTERNAL_SEARCH");
                player.setIsOfficialPhoto(false);
                playerRepository.save(player);
                log.info("Athlete photo resolved via TheSportsDB (EXTERNAL_SEARCH) for playerId={}: '{}'", playerId, player.getCommonName());
                return new MediaAsset(bytes, "image/png", "png");
            }
        }

        // Tier 3: Procedural Vector SVG Avatar Generator
        Integer jerseyNumber = null;
        List<MdmTeamMembership> memberships = membershipRepository.findByPlayerIdAndIsActiveTrue(playerId);
        if (!memberships.isEmpty() && memberships.get(0).getJerseyNumber() != null) {
            jerseyNumber = memberships.get(0).getJerseyNumber();
        }

        String flagEmoji = null;
        if (player.getNationality() != null) {
            flagEmoji = player.getNationality().getFlagCode();
        }

        String svg = athleteAvatarGenerator.generateSvgAvatar(player.getCommonName(), jerseyNumber, sportSlug, flagEmoji);
        byte[] svgBytes = svg.getBytes(StandardCharsets.UTF_8);
        storageService.saveAsset("players", playerId, "svg", svgBytes);

        player.setPhotoUrl("/api/v1/mdm/media/players/" + playerId + "/photo");
        player.setPhotoSource("GENERATED");
        player.setIsOfficialPhoto(false);
        playerRepository.save(player);
        log.info("Athlete avatar generated procedurally (GENERATED) for playerId={}: '{}'", playerId, player.getCommonName());

        return new MediaAsset(svgBytes, "image/svg+xml", "svg");
    }

    // ── External Lookup Helpers ──────────────────────────────────────────────

    private String searchTheSportsDbTeamBadge(String teamName) {
        if (teamName == null || teamName.isBlank()) return null;
        try {
            String encoded = URLEncoder.encode(teamName, StandardCharsets.UTF_8);
            String url = TSDB_TEAM_SEARCH + encoded;
            byte[] jsonBytes = fetchImageBytes(url);
            if (jsonBytes == null) return null;

            JsonNode root = objectMapper.readTree(jsonBytes);
            JsonNode teams = root.path("teams");
            if (teams.isArray() && !teams.isEmpty()) {
                JsonNode first = teams.get(0);
                String badge = first.path("strTeamBadge").asText(null);
                if (badge != null && !badge.isBlank()) {
                    return badge.contains("/preview") ? badge : badge + "/preview";
                }
            }
        } catch (Exception e) {
            log.debug("TheSportsDB team search failed for '{}': {}", teamName, e.getMessage());
        }
        return null;
    }

    private String searchTheSportsDbPlayerPhoto(String playerName) {
        if (playerName == null || playerName.isBlank()) return null;
        try {
            String encoded = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
            String url = TSDB_PLAYER_SEARCH + encoded;
            byte[] jsonBytes = fetchImageBytes(url);
            if (jsonBytes == null) return null;

            JsonNode root = objectMapper.readTree(jsonBytes);
            JsonNode player = root.path("player");
            if (player.isArray() && !player.isEmpty()) {
                JsonNode first = player.get(0);
                String cutout = first.path("strCutout").asText(null);
                if (cutout != null && !cutout.isBlank()) return cutout;
                String thumb = first.path("strThumb").asText(null);
                if (thumb != null && !thumb.isBlank()) {
                    return thumb.contains("/preview") ? thumb : thumb + "/preview";
                }
            }
        } catch (Exception e) {
            log.debug("TheSportsDB player search failed for '{}': {}", playerName, e.getMessage());
        }
        return null;
    }

    private byte[] fetchImageBytes(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");

            int status = conn.getResponseCode();
            if (status != 200) {
                return null;
            }

            try (InputStream is = conn.getInputStream()) {
                return is.readNBytes(MAX_IMAGE_BYTES);
            }
        } catch (Exception e) {
            log.debug("Failed to fetch image from {}: {}", url, e.getMessage());
            return null;
        }
    }
}
