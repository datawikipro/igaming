package pro.datawiki.igaming.playerfaces.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.playerfaces.domain.PlayerFace;
import pro.datawiki.igaming.playerfaces.domain.SportType;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing player avatar asset caching, WebP CDN simulation,
 * and high-tech Cyberpunk vector SVG avatar generation for fallbacks.
 */
@Slf4j
@Service
public class PlayerAvatarCdnService {

    private final Map<String, byte[]> assetCache = new ConcurrentHashMap<>();
    private final Map<String, String> mimeTypeCache = new ConcurrentHashMap<>();

    /**
     * Retrieves cached asset by filename, or generates a dynamic Cyberpunk SVG if missing.
     */
    public byte[] getAvatar(String filename) {
        if (filename == null || filename.isBlank()) {
            return generateCyberpunkSvg("Player", SportType.TENNIS, null, null).getBytes(StandardCharsets.UTF_8);
        }

        byte[] cached = assetCache.get(filename);
        if (cached != null) {
            return cached;
        }

        // If not cached, extract player slug and generate cyberpunk avatar SVG
        String baseName = filename.replace(".svg", "").replace(".webp", "").replace(".png", "");
        String displayName = baseName.replace("-", " ").replace("_", " ");
        // Title-case
        String[] parts = displayName.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase()).append(" ");
            }
        }
        String cleanName = sb.toString().trim();
        String svg = generateCyberpunkSvg(cleanName.isEmpty() ? "SmartBet Athlete" : cleanName, SportType.TENNIS, null, null);
        byte[] bytes = svg.getBytes(StandardCharsets.UTF_8);

        assetCache.put(filename, bytes);
        mimeTypeCache.put(filename, filename.endsWith(".webp") ? "image/webp" : "image/svg+xml");
        return bytes;
    }

    /**
     * Determines MIME content type for the asset.
     */
    public String getContentType(String filename) {
        if (filename == null) {
            return "image/svg+xml";
        }
        if (mimeTypeCache.containsKey(filename)) {
            return mimeTypeCache.get(filename);
        }
        if (filename.endsWith(".webp")) {
            return "image/webp";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "image/svg+xml";
    }

    /**
     * Stores image asset in the CDN cache.
     */
    public void cacheImage(String filename, byte[] data, String contentType) {
        if (filename != null && data != null) {
            assetCache.put(filename, data);
            if (contentType != null) {
                mimeTypeCache.put(filename, contentType);
            }
        }
    }

    /**
     * Checks if asset is stored in the cache.
     */
    public boolean hasAsset(String filename) {
        return assetCache.containsKey(filename);
    }

    /**
     * Generates a relative CDN URL for a given athlete.
     */
    public String buildCdnUrl(String playerName) {
        String slug = PlayerFace.normalizeName(playerName).replace(" ", "-");
        if (slug.isBlank()) {
            slug = "athlete";
        }
        return "/cdn/avatars/" + slug + ".svg";
    }

    /**
     * Generates a sleek, futuristic Cyberpunk styled SVG vector avatar.
     * Includes gold neon border for top seeds, cyan neon border for standard athletes,
     * glowing circuits, athlete monogram, and tournament seed badge.
     */
    public String generateCyberpunkSvg(String playerName, SportType sport, Integer seedNumber, Integer ranking) {
        String name = (playerName != null && !playerName.isBlank()) ? playerName.trim() : "Player";
        String initials = extractInitials(name);

        boolean isTopSeed = (seedNumber != null && seedNumber <= 5) || (ranking != null && ranking <= 5);
        String primaryGlow = isTopSeed ? "#FFD700" : "#00F0FF";
        String secondaryGlow = isTopSeed ? "#FF8800" : "#0077FF";
        String accentGradient = isTopSeed ? "goldGrad" : "cyanGrad";

        String seedBadgeXml = "";
        if (seedNumber != null && seedNumber > 0) {
            seedBadgeXml = """
                    <g transform="translate(18, 22)">
                        <rect x="0" y="0" width="34" height="22" rx="6" fill="#0A0E17" stroke="%s" stroke-width="1.5" />
                        <text x="17" y="15" fill="%s" font-family="'JetBrains Mono', 'Fira Code', monospace" font-size="12" font-weight="900" text-anchor="middle">[%d]</text>
                    </g>
                    """.formatted(primaryGlow, primaryGlow, seedNumber);
        }

        String sportIconXml = switch (sport != null ? sport : SportType.TENNIS) {
            case MMA, BOXING -> """
                    <!-- Combat Glove Icon -->
                    <path d="M120 180 C110 170, 110 155, 120 145 C125 140, 135 140, 140 145 C145 150, 150 150, 155 145 C160 140, 170 140, 175 145 C185 155, 185 170, 175 180 Z" fill="none" stroke="%s" stroke-width="1.5" opacity="0.6"/>
                    """.formatted(primaryGlow);
            default -> """
                    <!-- Tennis Racket Minimalist Motif -->
                    <circle cx="150" cy="165" r="14" fill="none" stroke="%s" stroke-width="1.5" opacity="0.5" />
                    <line x1="150" y1="179" x2="150" y2="192" stroke="%s" stroke-width="2" opacity="0.5" />
                    """.formatted(primaryGlow, primaryGlow);
        };

        return """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200" width="100%%" height="100%%">
                    <defs>
                        <linearGradient id="bgGrad" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                            <stop offset="0%%" stop-color="#070A10" />
                            <stop offset="50%%" stop-color="#0E1626" />
                            <stop offset="100%%" stop-color="#05080E" />
                        </linearGradient>
                        <linearGradient id="cyanGrad" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                            <stop offset="0%%" stop-color="#00F0FF" />
                            <stop offset="100%%" stop-color="#0066FF" />
                        </linearGradient>
                        <linearGradient id="goldGrad" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                            <stop offset="0%%" stop-color="#FFF275" />
                            <stop offset="100%%" stop-color="#FF9900" />
                        </linearGradient>
                        <linearGradient id="textGrad" x1="0%%" y1="0%%" x2="0%%" y2="100%%">
                            <stop offset="0%%" stop-color="#FFFFFF" />
                            <stop offset="100%%" stop-color="#A5B4FC" />
                        </linearGradient>
                        <filter id="neonGlow" x="-20%%" y="-20%%" width="140%%" height="140%%">
                            <feGaussianBlur stdDeviation="4" result="blur" />
                            <feMerge>
                                <feMergeNode in="blur" />
                                <feMergeNode in="blur" />
                                <feMergeNode in="SourceGraphic" />
                            </feMerge>
                        </filter>
                    </defs>

                    <!-- Background Base -->
                    <rect width="200" height="200" rx="36" fill="url(#bgGrad)" />

                    <!-- Cyberpunk Carbon Mesh Grid -->
                    <pattern id="grid" width="16" height="16" patternUnits="userSpaceOnUse">
                        <path d="M 16 0 L 0 0 0 16" fill="none" stroke="#1E293B" stroke-width="0.8" opacity="0.6"/>
                    </pattern>
                    <rect width="200" height="200" rx="36" fill="url(#grid)" />

                    <!-- Outer Glowing Hexagonal / Circular Cyber Ring -->
                    <circle cx="100" cy="100" r="82" fill="none" stroke="%s" stroke-width="1.8" opacity="0.3" />
                    <circle cx="100" cy="100" r="76" fill="#0C121E" stroke="url(#%s)" stroke-width="3" filter="url(#neonGlow)" />

                    <!-- Cyber Accent Tech Lines -->
                    <path d="M 28 100 L 40 100 M 160 100 L 172 100 M 100 28 L 100 40 M 100 160 L 100 172" stroke="%s" stroke-width="2" opacity="0.8"/>
                    <circle cx="44" cy="100" r="2" fill="%s" />
                    <circle cx="156" cy="100" r="2" fill="%s" />

                    <!-- Sport Motif in lower part -->
                    %s

                    <!-- Initials Monogram -->
                    <text x="100" y="116"
                          fill="url(#textGrad)"
                          font-family="'Montserrat', 'Inter', -apple-system, sans-serif"
                          font-size="46"
                          font-weight="900"
                          letter-spacing="2"
                          text-anchor="middle"
                          filter="drop-shadow(0 2px 8px rgba(0,0,0,0.8))">%s</text>

                    <!-- Player Surname Banner -->
                    <rect x="36" y="142" width="128" height="22" rx="4" fill="#080C14" stroke="%s" stroke-width="1" opacity="0.9" />
                    <text x="100" y="157"
                          fill="#E2E8F0"
                          font-family="'Inter', sans-serif"
                          font-size="10"
                          font-weight="700"
                          letter-spacing="1.2"
                          text-anchor="middle">%s</text>

                    <!-- Seed Badge (if present) -->
                    %s
                </svg>
                """.formatted(
                primaryGlow,
                accentGradient,
                secondaryGlow,
                primaryGlow,
                primaryGlow,
                sportIconXml,
                initials,
                primaryGlow,
                formatShortName(name),
                seedBadgeXml
        );
    }

    private String extractInitials(String name) {
        if (name == null || name.isBlank()) {
            return "SB";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            String single = parts[0];
            return single.length() >= 2 ? single.substring(0, 2).toUpperCase() : single.toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String formatShortName(String name) {
        if (name == null || name.isBlank()) {
            return "SMARTBET";
        }
        String[] parts = name.trim().split("\\s+");
        String surname = parts[parts.length - 1].toUpperCase();
        return surname.length() > 14 ? surname.substring(0, 14) : surname;
    }
}
