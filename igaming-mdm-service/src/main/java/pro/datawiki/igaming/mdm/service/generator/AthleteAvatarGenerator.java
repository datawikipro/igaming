package pro.datawiki.igaming.mdm.service.generator;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Procedural Vector SVG Avatar & Portrait Generator for sports athletes.
 * Produces crisp, institutional-grade player silhouettes and jersey emblems
 * when official portrait photos are not available, ensuring 100% visual coverage.
 */
@Component
public class AthleteAvatarGenerator {

    private static final List<String[]> PALETTES = List.of(
            new String[]{"#0ea5e9", "#0284c7", "#ffffff"}, // Sky Blue & Cyan
            new String[]{"#ef4444", "#b91c1c", "#ffffff"}, // Ruby Red
            new String[]{"#10b981", "#047857", "#ffffff"}, // Emerald Green
            new String[]{"#f59e0b", "#d97706", "#ffffff"}, // Amber Gold
            new String[]{"#8b5cf6", "#6d28d9", "#ffffff"}, // Vivid Violet
            new String[]{"#ec4899", "#be185d", "#ffffff"}, // Neon Pink
            new String[]{"#06b6d4", "#0e7490", "#ffffff"}, // Dark Cyan
            new String[]{"#f97316", "#c2410c", "#ffffff"}  // Sunset Orange
    );

    /**
     * Generates an SVG vector avatar of an athlete jersey silhouette with number/initials and flag.
     */
    public String generateSvgAvatar(String playerName, Integer shirtNumber, String sport, String flagEmoji) {
        String safeName = (playerName == null || playerName.isBlank()) ? "Athlete" : playerName.trim();
        String initials = extractInitials(safeName);

        int hash = Math.abs(safeName.hashCode());
        String[] palette = PALETTES.get(hash % PALETTES.size());
        String primaryColor = palette[0];
        String secondaryColor = palette[1];
        String textColor = palette[2];

        String sportIcon = resolveSportEmoji(sport);
        String centerText = (shirtNumber != null && shirtNumber > 0 && shirtNumber <= 99) 
                ? String.valueOf(shirtNumber) 
                : initials;

        String safeFlag = flagEmoji != null ? flagEmoji : "";

        return """
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200" width="200" height="200">
  <defs>
    <linearGradient id="bgGrad" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
      <stop offset="0%%" stop-color="#0f172a" />
      <stop offset="100%%" stop-color="#020617" />
    </linearGradient>
    <linearGradient id="jerseyGrad" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
      <stop offset="0%%" stop-color="%s" />
      <stop offset="100%%" stop-color="%s" />
    </linearGradient>
    <filter id="glow" x="-20%%" y="-20%%" width="140%%" height="140%%">
      <feDropShadow dx="0" dy="2" stdDeviation="4" flood-color="%s" flood-opacity="0.4"/>
    </filter>
  </defs>

  <!-- Background Circle -->
  <circle cx="100" cy="100" r="94" fill="url(#bgGrad)" stroke="%s" stroke-width="3" stroke-opacity="0.6"/>

  <!-- Head / Neck Silhouette -->
  <circle cx="100" cy="58" r="26" fill="%s" fill-opacity="0.25" stroke="%s" stroke-width="1.5" />

  <!-- Jersey Torso Silhouette -->
  <path d="M 52 165 C 52 110, 70 96, 85 92 L 100 102 L 115 92 C 130 96, 148 110, 148 165 Z"
        fill="url(#jerseyGrad)"
        stroke="%s"
        stroke-width="2"
        filter="url(#glow)" />

  <!-- Jersey Collar Accent -->
  <path d="M 85 92 C 95 106, 105 106, 115 92" fill="none" stroke="%s" stroke-width="2.5" stroke-linecap="round" />

  <!-- Jersey Center Number / Monogram -->
  <text x="100" y="138"
        font-family="'Orbitron', 'Montserrat', 'Arial Black', sans-serif"
        font-size="%d"
        font-weight="900"
        letter-spacing="1"
        text-anchor="middle"
        dominant-baseline="central"
        fill="%s"
        stroke="#000000"
        stroke-width="0.5">%s</text>

  <!-- Sport Badge Top Right -->
  <circle cx="156" cy="44" r="18" fill="#1e293b" stroke="%s" stroke-width="1.5" />
  <text x="156" y="47" font-size="14" text-anchor="middle" dominant-baseline="central">%s</text>

  <!-- Flag Badge Bottom Right (if present) -->
  %s
</svg>
""".formatted(
                primaryColor,
                secondaryColor,
                primaryColor,
                primaryColor,
                primaryColor,
                primaryColor,
                secondaryColor,
                textColor,
                centerText.length() > 2 ? 22 : 28,
                textColor,
                centerText,
                primaryColor,
                sportIcon,
                safeFlag.isBlank() ? "" : """
  <g transform="translate(138, 140)">
    <circle cx="18" cy="18" r="18" fill="#0f172a" stroke="%s" stroke-width="1.5" />
    <text x="18" y="22" font-size="16" text-anchor="middle" dominant-baseline="central">%s</text>
  </g>
  """.formatted(primaryColor, safeFlag)
        );
    }

    /**
     * Returns a ready-to-use Base64 data URL for direct embedding in <img src="...">.
     */
    public String generateDataUrl(String playerName, Integer shirtNumber, String sport, String flagEmoji) {
        String svg = generateSvgAvatar(playerName, shirtNumber, sport, flagEmoji);
        String base64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64;
    }

    private String extractInitials(String name) {
        String clean = name.replaceAll("[^a-zA-Zа-яА-Я0-9\\s]", "").trim();
        if (clean.isBlank()) return "AT";
        String[] parts = clean.split("\\s+");
        if (parts.length == 1) {
            return parts[0].length() <= 2 ? parts[0].toUpperCase() : parts[0].substring(0, 2).toUpperCase();
        } else {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }

    private String resolveSportEmoji(String sport) {
        if (sport == null) return "⚡";
        String s = sport.toUpperCase();
        if (s.contains("FOOTBALL") || s.contains("SOCCER")) return "⚽";
        if (s.contains("BASKET")) return "🏀";
        if (s.contains("HOCKEY")) return "🏒";
        if (s.contains("TENNIS")) return "🎾";
        if (s.contains("ESPORT") || s.contains("CS") || s.contains("DOTA")) return "🎮";
        if (s.contains("VOLLEY")) return "🏐";
        if (s.contains("BASEBALL")) return "⚾";
        if (s.contains("RUGBY") || s.contains("AMERICAN")) return "🏈";
        if (s.contains("MMA") || s.contains("BOX")) return "🥊";
        return "🏆";
    }
}
