package pro.datawiki.igaming.capture.sofascore.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility for detecting whether a sports team is a national (representative) team
 * and resolving ISO-3166-1 alpha-2 country codes and flag URLs.
 *
 * <h3>Detection strategies (applied in order):</h3>
 * <ol>
 *   <li><b>SofaScore API flag</b>: {@code team.national == true} in JSON response.</li>
 *   <li><b>Category alpha2</b>: If the SofaScore tournament category has a direct
 *       ISO alpha-2 code and the team belongs to a national competition.</li>
 *   <li><b>Name heuristic</b>: Keyword matching against known country names in EN/RU.</li>
 * </ol>
 */
@Slf4j
@UtilityClass
public class NationalTeamDetector {

    private static final String SOFASCORE_FLAG_URL =
            "https://api.sofascore.app/api/v1/team/{teamId}/image";

    /**
     * ISO-3166-1 alpha-2 country code lookup.
     * Key: lowercase English country name (or common alias).
     * Value: ISO-3166-1 alpha-2 code (uppercase).
     */
    private static final Map<String, String> COUNTRY_TO_ISO = new HashMap<>(180);

    /**
     * Name-based heuristics: set of normalized substrings that indicate national team names.
     * Covers major football/sports national team naming patterns in English and Russian.
     */
    private static final Set<String> NATIONAL_TEAM_KEYWORDS = Set.of(
            // Generic suffixes / patterns
            "national", "nationals", "national team", "select", "u21", "u-21", "u20", "u-20",
            "u17", "u-17", "u19", "u-19", "u23", "u-23", "olympic", "olympics",
            // Russian suffixes
            "сборная", "олимпийская"
    );

    static {
        // ── Europe ────────────────────────────────────────────────────────────
        put("albania", "AL");
        put("andorra", "AD");
        put("armenia", "AM");
        put("austria", "AT");
        put("azerbaijan", "AZ");
        put("belarus", "BY");
        put("belgium", "BE");
        put("bosnia", "BA"); put("bosnia and herzegovina", "BA");
        put("bulgaria", "BG");
        put("croatia", "HR");
        put("cyprus", "CY");
        put("czech republic", "CZ"); put("czechia", "CZ");
        put("denmark", "DK");
        put("england", "EN"); // FIFA uses GB-ENG; we use custom EN
        put("estonia", "EE");
        put("faroe islands", "FO");
        put("finland", "FI");
        put("france", "FR");
        put("georgia", "GE");
        put("germany", "DE");
        put("gibraltar", "GI");
        put("greece", "GR");
        put("hungary", "HU");
        put("iceland", "IS");
        put("ireland", "IE"); put("republic of ireland", "IE");
        put("israel", "IL");
        put("italy", "IT");
        put("kazakhstan", "KZ");
        put("kosovo", "XK");
        put("latvia", "LV");
        put("liechtenstein", "LI");
        put("lithuania", "LT");
        put("luxembourg", "LU");
        put("malta", "MT");
        put("moldova", "MD");
        put("monaco", "MC");
        put("montenegro", "ME");
        put("netherlands", "NL"); put("holland", "NL");
        put("north macedonia", "MK"); put("macedonia", "MK");
        put("northern ireland", "XI"); // FIFA code
        put("norway", "NO");
        put("poland", "PL");
        put("portugal", "PT");
        put("romania", "RO");
        put("russia", "RU"); put("russian federation", "RU");
        put("san marino", "SM");
        put("scotland", "SC"); // FIFA: GB-SCT
        put("serbia", "RS");
        put("slovakia", "SK");
        put("slovenia", "SI");
        put("spain", "ES");
        put("sweden", "SE");
        put("switzerland", "CH");
        put("turkey", "TR"); put("turkiye", "TR");
        put("ukraine", "UA");
        put("wales", "WA"); // FIFA: GB-WLS

        // ── Americas ─────────────────────────────────────────────────────────
        put("argentina", "AR");
        put("bolivia", "BO");
        put("brazil", "BR"); put("brasil", "BR");
        put("canada", "CA");
        put("chile", "CL");
        put("colombia", "CO");
        put("costa rica", "CR");
        put("cuba", "CU");
        put("ecuador", "EC");
        put("el salvador", "SV");
        put("guatemala", "GT");
        put("haiti", "HT");
        put("honduras", "HN");
        put("jamaica", "JM");
        put("mexico", "MX");
        put("nicaragua", "NI");
        put("panama", "PA");
        put("paraguay", "PY");
        put("peru", "PE");
        put("trinidad and tobago", "TT"); put("trinidad", "TT");
        put("united states", "US"); put("usa", "US"); put("united states of america", "US");
        put("uruguay", "UY");
        put("venezuela", "VE");

        // ── Asia ──────────────────────────────────────────────────────────────
        put("afghanistan", "AF");
        put("bahrain", "BH");
        put("china", "CN"); put("china pr", "CN"); put("china pr.", "CN");
        put("hong kong", "HK");
        put("india", "IN");
        put("indonesia", "ID");
        put("iran", "IR"); put("islamic republic of iran", "IR");
        put("iraq", "IQ");
        put("japan", "JP");
        put("jordan", "JO");
        put("kuwait", "KW");
        put("kyrgyzstan", "KG");
        put("laos", "LA");
        put("lebanon", "LB");
        put("malaysia", "MY");
        put("maldives", "MV");
        put("mongolia", "MN");
        put("myanmar", "MM");
        put("nepal", "NP");
        put("north korea", "KP"); put("korea dpr", "KP");
        put("oman", "OM");
        put("pakistan", "PK");
        put("palestine", "PS");
        put("philippines", "PH");
        put("qatar", "QA");
        put("saudi arabia", "SA");
        put("singapore", "SG");
        put("south korea", "KR"); put("korea republic", "KR"); put("korea", "KR");
        put("sri lanka", "LK");
        put("syria", "SY");
        put("taiwan", "TW");
        put("tajikistan", "TJ");
        put("thailand", "TH");
        put("timor-leste", "TL");
        put("turkmenistan", "TM");
        put("united arab emirates", "AE"); put("uae", "AE");
        put("uzbekistan", "UZ");
        put("vietnam", "VN");
        put("yemen", "YE");

        // ── Africa ────────────────────────────────────────────────────────────
        put("algeria", "DZ");
        put("angola", "AO");
        put("cameroon", "CM");
        put("cape verde", "CV");
        put("democratic republic of congo", "CD"); put("dr congo", "CD"); put("congo dr", "CD");
        put("egypt", "EG");
        put("ethiopia", "ET");
        put("ghana", "GH");
        put("ivory coast", "CI"); put("cote d'ivoire", "CI"); put("cote divoire", "CI");
        put("kenya", "KE");
        put("mali", "ML");
        put("morocco", "MA");
        put("nigeria", "NG");
        put("senegal", "SN");
        put("south africa", "ZA");
        put("tanzania", "TZ");
        put("togo", "TG");
        put("tunisia", "TN");
        put("uganda", "UG");
        put("zambia", "ZM");
        put("zimbabwe", "ZW");

        // ── Oceania ───────────────────────────────────────────────────────────
        put("australia", "AU");
        put("fiji", "FJ");
        put("new zealand", "NZ");
        put("papua new guinea", "PG");
        put("solomon islands", "SB");
        put("vanuatu", "VU");

        // ── Russian aliases (common in RU-locale bookmakers) ─────────────────
        put("австрия", "AT");
        put("азербайджан", "AZ");
        put("алжир", "DZ");
        put("ангола", "AO");
        put("аргентина", "AR");
        put("армения", "AM");
        put("беларусь", "BY"); put("белоруссия", "BY");
        put("бельгия", "BE");
        put("боливия", "BO");
        put("босния", "BA"); put("босния и герцеговина", "BA");
        put("бразилия", "BR");
        put("великобритания", "GB");
        put("венгрия", "HU");
        put("венесуэла", "VE");
        put("германия", "DE");
        put("голландия", "NL"); put("нидерланды", "NL");
        put("греция", "GR");
        put("грузия", "GE");
        put("дания", "DK");
        put("египет", "EG");
        put("израиль", "IL");
        put("индия", "IN");
        put("иран", "IR");
        put("ирак", "IQ");
        put("ирландия", "IE");
        put("испания", "ES");
        put("италия", "IT");
        put("камерун", "CM");
        put("канада", "CA");
        put("катар", "QA");
        put("китай", "CN");
        put("колумбия", "CO");
        put("корея", "KR"); put("южная корея", "KR"); put("северная корея", "KP");
        put("кот д'ивуар", "CI"); put("кот-д'ивуар", "CI");
        put("латвия", "LV");
        put("литва", "LT");
        put("марокко", "MA");
        put("мексика", "MX");
        put("нигерия", "NG");
        put("норвегия", "NO");
        put("парагвай", "PY");
        put("перу", "PE");
        put("польша", "PL");
        put("португалия", "PT");
        put("россия", "RU"); put("российская федерация", "RU");
        put("румыния", "RO");
        put("саудовская аравия", "SA");
        put("сенегал", "SN");
        put("сербия", "RS");
        put("словакия", "SK");
        put("словения", "SI");
        put("сша", "US"); put("соединённые штаты", "US"); put("соединенные штаты", "US");
        put("тунис", "TN");
        put("туркмения", "TM"); put("туркменистан", "TM");
        put("турция", "TR");
        put("уругвай", "UY");
        put("узбекистан", "UZ");
        put("украина", "UA");
        put("финляндия", "FI");
        put("франция", "FR");
        put("хорватия", "HR");
        put("чехия", "CZ"); put("чешская республика", "CZ");
        put("чили", "CL");
        put("швейцария", "CH");
        put("швеция", "SE");
        put("эквадор", "EC");
        put("эстония", "EE");
        put("япония", "JP");
    }

    private static void put(String name, String code) {
        COUNTRY_TO_ISO.put(name.toLowerCase(), code);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Detects national team status from SofaScore API JSON team node.
     *
     * <p>SofaScore provides a {@code team.national} boolean field.
     * This is the most reliable detection method.
     *
     * @param teamNode the {@code homeTeam} or {@code awayTeam} JsonNode from SofaScore scheduled-events API
     * @return true if the team is a national team
     */
    public static boolean isNationalTeamFromApi(JsonNode teamNode) {
        if (teamNode == null || teamNode.isMissingNode()) return false;
        JsonNode nationalNode = teamNode.path("national");
        if (!nationalNode.isMissingNode() && nationalNode.isBoolean()) {
            return nationalNode.asBoolean(false);
        }
        // Fallback: check "type" field; some SofaScore API versions return type="national"
        String type = teamNode.path("type").asText("");
        return "national".equalsIgnoreCase(type);
    }

    /**
     * Detects national team status from a team name using keyword and country-name heuristics.
     * Use as fallback when the API does not provide a direct flag.
     *
     * @param teamName the team display name (English or Russian)
     * @return true if the name matches national team patterns
     */
    public static boolean isNationalTeamByName(String teamName) {
        if (teamName == null || teamName.isBlank()) return false;
        String lower = teamName.toLowerCase().trim();

        // Direct country name match (e.g. "France", "Germany")
        if (COUNTRY_TO_ISO.containsKey(lower)) return true;

        // Keyword match (e.g. "France U21", "England National")
        for (String keyword : NATIONAL_TEAM_KEYWORDS) {
            if (lower.contains(keyword)) return true;
        }

        // Country name prefix/suffix (e.g. "Argentina Women", "Brazil Olympic")
        for (String countryName : COUNTRY_TO_ISO.keySet()) {
            if (lower.startsWith(countryName + " ") || lower.endsWith(" " + countryName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Resolves ISO-3166-1 alpha-2 code.
     * Priority:
     * <ol>
     *   <li>SofaScore category {@code alpha2} field (most authoritative)</li>
     *   <li>Team's {@code country.alpha2} field in the team node</li>
     *   <li>Name-based lookup from {@code COUNTRY_TO_ISO}</li>
     * </ol>
     *
     * @param teamNode   the team JSON node
     * @param teamName   the team display name for fallback lookup
     * @param categoryNode the tournament category JSON node (may contain alpha2)
     * @return ISO-3166-1 alpha-2 code or null if unresolvable
     */
    public static String resolveCountryCode(JsonNode teamNode, String teamName, JsonNode categoryNode) {
        // 1. Category alpha2 — most authoritative for national competitions
        if (categoryNode != null && !categoryNode.isMissingNode()) {
            String alpha2 = categoryNode.path("alpha2").asText(null);
            if (alpha2 != null && alpha2.length() == 2) {
                return alpha2.toUpperCase();
            }
        }

        // 2. Team country alpha2 field
        if (teamNode != null && !teamNode.isMissingNode()) {
            String alpha2 = teamNode.path("country").path("alpha2").asText(null);
            if (alpha2 != null && alpha2.length() == 2) {
                return alpha2.toUpperCase();
            }
            // Team country name fallback
            String countryName = teamNode.path("country").path("name").asText(null);
            if (countryName != null) {
                String code = COUNTRY_TO_ISO.get(countryName.toLowerCase().trim());
                if (code != null) return code;
            }
        }

        // 3. Name-based lookup
        if (teamName != null && !teamName.isBlank()) {
            String lower = teamName.toLowerCase().trim();
            // Direct match
            String code = COUNTRY_TO_ISO.get(lower);
            if (code != null) return code;
            // Prefix/suffix match
            for (Map.Entry<String, String> entry : COUNTRY_TO_ISO.entrySet()) {
                if (lower.startsWith(entry.getKey() + " ") || lower.endsWith(" " + entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Builds the SofaScore team image URL used as flag URL for national teams.
     * SofaScore stores country emblems under the team image endpoint.
     *
     * @param externalTeamId SofaScore team ID
     * @return full flag/emblem URL or null if teamId is blank
     */
    public static String buildFlagUrl(String externalTeamId) {
        if (externalTeamId == null || externalTeamId.isBlank()) return null;
        return "https://api.sofascore.app/api/v1/team/" + externalTeamId + "/image";
    }
}
