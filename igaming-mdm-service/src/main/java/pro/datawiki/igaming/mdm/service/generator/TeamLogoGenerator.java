package pro.datawiki.igaming.mdm.service.generator;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Procedural Vector SVG Crest & Logo Generator for sports teams.
 * Produces crisp, institutional-grade vector crests when official external badge images
 * are not available, ensuring 100% logo coverage with zero external latency and 0 cost.
 */
@Component
public class TeamLogoGenerator {

    // Harmonious modern sports palettes: [Primary Color, Secondary/Border Color, Text Color]
    private static final List<String[]> PALETTES = List.of(
            new String[]{"#0a2540", "#00d4ff", "#ffffff"}, // Deep Navy & Cyan
            new String[]{"#c8102e", "#ffffff", "#ffffff"}, // Crimson & White
            new String[]{"#1f3a93", "#f4d03f", "#ffffff"}, // Royal Blue & Gold
            new String[]{"#006400", "#ffd700", "#ffffff"}, // Emerald Green & Gold
            new String[]{"#4a154b", "#e01e5a", "#ffffff"}, // Plum & Rose
            new String[]{"#2c3e50", "#e74c3c", "#ffffff"}, // Slate & Scarlet
            new String[]{"#1a1a24", "#00e676", "#ffffff"}, // Obsidian & Cyber Green
            new String[]{"#d35400", "#2c3e50", "#ffffff"}, // Amber Orange & Navy
            new String[]{"#5b2c6f", "#a569bd", "#ffffff"}, // Imperial Purple & Lilac
            new String[]{"#00529b", "#c8102e", "#ffffff"}  // Blue & Red Classic
    );

    /**
     * Generates an SVG vector string of a classic team shield with monogram and sport icon.
     */
    public String generateSvgCrest(String teamName, String sport) {
        String safeName = (teamName == null || teamName.isBlank()) ? "FC" : teamName.trim();
        String monogram = extractMonogram(safeName);

        int hash = Math.abs(safeName.hashCode());
        String[] palette = PALETTES.get(hash % PALETTES.size());
        String primaryColor = palette[0];
        String secondaryColor = palette[1];
        String textColor = palette[2];

        String sportIcon = resolveSportEmoji(sport);

        return """
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 240" width="200" height="240">
  <defs>
    <linearGradient id="bgGrad" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
      <stop offset="0%%" stop-color="%s" />
      <stop offset="100%%" stop-color="#05070e" />
    </linearGradient>
    <linearGradient id="borderGrad" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
      <stop offset="0%%" stop-color="%s" />
      <stop offset="100%%" stop-color="%s" stop-opacity="0.4" />
    </linearGradient>
    <filter id="glow" x="-20%%" y="-20%%" width="140%%" height="140%%">
      <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="%s" flood-opacity="0.35"/>
    </filter>
  </defs>

  <!-- Outer Shield -->
  <path d="M 100 8 C 170 8 190 28 190 68 C 190 152 135 210 100 230 C 65 210 10 152 10 68 C 10 28 30 8 100 8 Z"
        fill="url(#bgGrad)"
        stroke="url(#borderGrad)"
        stroke-width="5"
        filter="url(#glow)" />

  <!-- Inner Inset Shield -->
  <path d="M 100 20 C 158 20 176 36 176 72 C 176 142 128 194 100 212 C 72 194 24 142 24 72 C 24 36 42 20 100 20 Z"
        fill="none"
        stroke="%s"
        stroke-width="1.5"
        stroke-opacity="0.3" />

  <!-- Sport Emblem Top -->
  <text x="100" y="62" font-size="24" text-anchor="middle" dominant-baseline="central">%s</text>

  <!-- Center Monogram -->
  <text x="100" y="118"
        font-family="'Orbitron', 'Montserrat', 'Arial Black', sans-serif"
        font-size="%d"
        font-weight="900"
        letter-spacing="1.5"
        text-anchor="middle"
        dominant-baseline="central"
        fill="%s"
        stroke="%s"
        stroke-width="0.5">%s</text>

  <!-- Bottom Stars or Accent -->
  <g fill="%s" opacity="0.85">
    <polygon points="100,162 103,170 111,170 105,175 107,183 100,178 93,183 95,175 89,170 97,170" transform="translate(0, 0)" />
    <polygon points="80,165 82,171 88,171 83,175 85,181 80,177 75,181 77,175 72,171 78,171" transform="scale(0.8) translate(22, 40)" />
    <polygon points="120,165 122,171 128,171 123,175 125,181 120,177 115,181 117,175 112,171 118,171" transform="scale(0.8) translate(28, 40)" />
  </g>
</svg>
""".formatted(
                primaryColor,
                secondaryColor,
                secondaryColor,
                secondaryColor,
                secondaryColor,
                sportIcon,
                monogram.length() > 3 ? 30 : 38,
                textColor,
                secondaryColor,
                monogram,
                secondaryColor
        );
    }

    /**
     * Returns a ready-to-use Base64 data URL for direct embedding in <img src="...">.
     */
    public String generateDataUrl(String teamName, String sport) {
        String svg = generateSvgCrest(teamName, sport);
        String base64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64;
    }

    private String extractMonogram(String name) {
        String clean = name.replaceAll("(?i)\\b(fc|fk|sc|cf|ac|bc|afc|club|team|national)\\b", "").trim();
        if (clean.isBlank()) {
            clean = name;
        }

        String[] parts = clean.split("\\s+");
        if (parts.length == 1) {
            String word = parts[0].replaceAll("[^a-zA-Zа-яА-Я0-9]", "");
            return word.length() <= 3 ? word.toUpperCase() : word.substring(0, 3).toUpperCase();
        } else if (parts.length == 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1) + parts[2].substring(0, 1)).toUpperCase();
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
