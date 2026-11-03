/*
 * Decompiled with CFR 0.152.
 */
package pro.datawiki.igaming.mdm.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import pro.datawiki.igaming.mdm.service.MdmMatchResolutionService;

public final class CountryNormalizer {
    private static final Map<String, CountryInfo> MAPPINGS = new HashMap<String, CountryInfo>();
    private static final Set<String> INVALID_COUNTRY_WORDS = Set.of("\u0444\u0443\u0442\u0431\u043e\u043b", "\u043a\u0438\u0431\u0435\u0440\u0444\u0443\u0442\u0431\u043e\u043b", "\u0442\u0435\u043d\u043d\u0438\u0441", "\u0445\u043e\u043a\u043a\u0435\u0439", "\u0431\u0430\u0441\u043a\u0435\u0442\u0431\u043e\u043b", "\u043a\u0438\u0431\u0435\u0440\u0431\u0430\u0441\u043a\u0435\u0442\u0431\u043e\u043b", "\u043a\u0438\u0431\u0435\u0440\u0441\u043f\u043e\u0440\u0442", "\u0432\u043e\u043b\u0435\u0439\u0431\u043e\u043b", "\u0433\u0430\u043d\u0434\u0431\u043e\u043b", "\u0431\u0435\u0439\u0441\u0431\u043e\u043b", "\u0440\u0435\u0433\u0431\u0438", "\u0441\u043d\u0443\u043a\u0435\u0440", "\u0434\u0430\u0440\u0442\u0441", "football", "soccer", "esports", "tennis", "hockey", "basketball", "volleyball", "\u043b\u0438\u0433\u0430", "\u043a\u0443\u0431\u043e\u043a", "\u0434\u0438\u0432\u0438\u0437\u0438\u043e\u043d", "\u0447\u0435\u043c\u043f\u0438\u043e\u043d\u0430\u0442", "\u0442\u0443\u0440\u043d\u0438\u0440", "\u043f\u0435\u0440\u0432\u0435\u043d\u0441\u0442\u0432\u043e", "\u0441\u0435\u0440\u0438\u044f", "league", "cup", "division", "championship", "tournament", "\u043c\u0443\u0436\u0447\u0438\u043d\u044b", "\u0436\u0435\u043d\u0449\u0438\u043d\u044b", "\u043c\u043e\u043b", "\u043c\u043e\u043b\u043e\u0434\u0435\u0436\u043d\u0430\u044f", "\u0440\u0435\u0437\u0435\u0440\u0432", "\u0441\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430", "men", "women", "youth", "reserve", "statistics", "stats", "1-\u0439 \u0434\u0438\u0432\u0438\u0437\u0438\u043e\u043d", "2-\u0439 \u0434\u0438\u0432\u0438\u0437\u0438\u043e\u043d", "1-\u044f \u043b\u0438\u0433\u0430", "2-\u044f \u043b\u0438\u0433\u0430", "\u0447\u0435\u043c\u043f\u0438\u043e\u043d\u0430\u0442 1", "\u0447\u0435\u043c\u043f\u0438\u043e\u043d\u0430\u0442 2");

    private static void addCountry(String slug, String ruName, String code) {
        CountryInfo info = new CountryInfo(slug, code, false);
        MAPPINGS.put(slug, info);
        MAPPINGS.put(ruName, info);
    }

    private CountryNormalizer() {
    }

    public static CountryInfo normalize(String countryName, Boolean isInternational) {
        if (Boolean.TRUE.equals(isInternational)) {
            return MAPPINGS.get("international");
        }
        if (countryName == null || countryName.isBlank()) {
            return null;
        }
        String clean = countryName.trim().toLowerCase();
        if (INVALID_COUNTRY_WORDS.contains(clean)) {
            return null;
        }
        CountryInfo exact = MAPPINGS.get(clean);
        if (exact != null) {
            return exact;
        }
        String slug = MdmMatchResolutionService.toSlug(clean);
        CountryInfo bySlug = MAPPINGS.get(slug);
        if (bySlug != null) {
            return bySlug;
        }
        return null;
    }

    static {
        CountryInfo intl = new CountryInfo("international", "INT", true);
        MAPPINGS.put("international", intl);
        MAPPINGS.put("world", intl);
        MAPPINGS.put("\u043c\u0438\u0440", intl);
        MAPPINGS.put("\u043c\u0435\u0436\u0434\u0443\u043d\u0430\u0440\u043e\u0434\u043d\u044b\u0435", intl);
        MAPPINGS.put("\u043c\u0435\u0436\u0434\u0443\u043d\u0430\u0440\u043e\u0434\u043d\u044b\u0439", intl);
        MAPPINGS.put("\u0435\u0432\u0440\u043e\u043a\u0443\u0431\u043a\u0438", intl);
        MAPPINGS.put("europe", intl);
        MAPPINGS.put("\u0435\u0432\u0440\u043e\u043f\u0430", intl);
        MAPPINGS.put("\u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0448\u043d\u043b", intl);
        MAPPINGS.put("\u0433\u043b\u043e\u0431\u0430\u043b\u044c\u043d\u044b\u0439", intl);
        CountryNormalizer.addCountry("portugal", "\u043f\u043e\u0440\u0442\u0443\u0433\u0430\u043b\u0438\u044f", "POR");
        CountryNormalizer.addCountry("england", "\u0430\u043d\u0433\u043b\u0438\u044f", "ENG");
        CountryNormalizer.addCountry("great-britain", "\u0432\u0435\u043b\u0438\u043a\u043e\u0431\u0440\u0438\u0442\u0430\u043d\u0438\u044f", "GBR");
        CountryNormalizer.addCountry("scotland", "\u0448\u043e\u0442\u043b\u0430\u043d\u0434\u0438\u044f", "SCO");
        CountryNormalizer.addCountry("wales", "\u0443\u044d\u043b\u044c\u0441", "WAL");
        CountryNormalizer.addCountry("northern-ireland", "\u0441\u0435\u0432\u0435\u0440\u043d\u0430\u044f \u0438\u0440\u043b\u0430\u043d\u0434\u0438\u044f", "NIR");
        CountryNormalizer.addCountry("ireland", "\u0438\u0440\u043b\u0430\u043d\u0434\u0438\u044f", "IRL");
        CountryNormalizer.addCountry("spain", "\u0438\u0441\u043f\u0430\u043d\u0438\u044f", "ESP");
        CountryNormalizer.addCountry("russia", "\u0440\u043e\u0441\u0441\u0438\u044f", "RUS");
        MAPPINGS.put("\u0440\u0444", MAPPINGS.get("russia"));
        CountryNormalizer.addCountry("germany", "\u0433\u0435\u0440\u043c\u0430\u043d\u0438\u044f", "GER");
        CountryNormalizer.addCountry("italy", "\u0438\u0442\u0430\u043b\u0438\u044f", "ITA");
        CountryNormalizer.addCountry("france", "\u0444\u0440\u0430\u043d\u0446\u0438\u044f", "FRA");
        CountryNormalizer.addCountry("netherlands", "\u043d\u0438\u0434\u0435\u0440\u043b\u0430\u043d\u0434\u044b", "NED");
        MAPPINGS.put("\u0433\u043e\u043b\u043b\u0430\u043d\u0434\u0438\u044f", MAPPINGS.get("netherlands"));
        MAPPINGS.put("holland", MAPPINGS.get("netherlands"));
        CountryNormalizer.addCountry("belgium", "\u0431\u0435\u043b\u044c\u0433\u0438\u044f", "BEL");
        CountryNormalizer.addCountry("switzerland", "\u0448\u0432\u0435\u0439\u0446\u0430\u0440\u0438\u044f", "SUI");
        CountryNormalizer.addCountry("austria", "\u0430\u0432\u0441\u0442\u0440\u0438\u044f", "AUT");
        CountryNormalizer.addCountry("sweden", "\u0448\u0432\u0435\u0446\u0438\u044f", "SWE");
        CountryNormalizer.addCountry("norway", "\u043d\u043e\u0440\u0432\u0435\u0433\u0438\u044f", "NOR");
        CountryNormalizer.addCountry("denmark", "\u0434\u0430\u043d\u0438\u044f", "DEN");
        CountryNormalizer.addCountry("finland", "\u0444\u0438\u043d\u043b\u044f\u043d\u0434\u0438\u044f", "FIN");
        CountryNormalizer.addCountry("iceland", "\u0438\u0441\u043b\u0430\u043d\u0434\u0438\u044f", "ISL");
        CountryNormalizer.addCountry("poland", "\u043f\u043e\u043b\u044c\u0448\u0430", "POL");
        CountryNormalizer.addCountry("czech-republic", "\u0447\u0435\u0445\u0438\u044f", "CZE");
        MAPPINGS.put("czechia", MAPPINGS.get("czech-republic"));
        CountryNormalizer.addCountry("slovakia", "\u0441\u043b\u043e\u0432\u0430\u043a\u0438\u044f", "SVK");
        CountryNormalizer.addCountry("hungary", "\u0432\u0435\u043d\u0433\u0440\u0438\u044f", "HUN");
        CountryNormalizer.addCountry("romania", "\u0440\u0443\u043c\u044b\u043d\u0438\u044f", "ROU");
        CountryNormalizer.addCountry("bulgaria", "\u0431\u043e\u043b\u0433\u0430\u0440\u0438\u044f", "BUL");
        CountryNormalizer.addCountry("greece", "\u0433\u0440\u0435\u0446\u0438\u044f", "GRE");
        CountryNormalizer.addCountry("turkey", "\u0442\u0443\u0440\u0446\u0438\u044f", "TUR");
        MAPPINGS.put("t\u00fcrkiye", MAPPINGS.get("turkey"));
        CountryNormalizer.addCountry("cyprus", "\u043a\u0438\u043f\u0440", "CYP");
        CountryNormalizer.addCountry("serbia", "\u0441\u0435\u0440\u0431\u0438\u044f", "SRB");
        CountryNormalizer.addCountry("croatia", "\u0445\u043e\u0440\u0432\u0430\u0442\u0438\u044f", "CRO");
        CountryNormalizer.addCountry("slovenia", "\u0441\u043b\u043e\u0432\u0435\u043d\u0438\u044f", "SVN");
        CountryNormalizer.addCountry("bosnia-and-herzegovina", "\u0431\u043e\u0441\u043d\u0438\u044f \u0438 \u0433\u0435\u0440\u0446\u0435\u0433\u043e\u0432\u0438\u043d\u0430", "BIH");
        MAPPINGS.put("\u0431\u043e\u0441\u043d\u0438\u044f", MAPPINGS.get("bosnia-and-herzegovina"));
        MAPPINGS.put("bosnia", MAPPINGS.get("bosnia-and-herzegovina"));
        CountryNormalizer.addCountry("albania", "\u0430\u043b\u0431\u0430\u043d\u0438\u044f", "ALB");
        CountryNormalizer.addCountry("north-macedonia", "\u0441\u0435\u0432\u0435\u0440\u043d\u0430\u044f \u043c\u0430\u043a\u0435\u0434\u043e\u043d\u0438\u044f", "MKD");
        MAPPINGS.put("\u043c\u0430\u043a\u0435\u0434\u043e\u043d\u0438\u044f", MAPPINGS.get("north-macedonia"));
        MAPPINGS.put("macedonia", MAPPINGS.get("north-macedonia"));
        CountryNormalizer.addCountry("montenegro", "\u0447\u0435\u0440\u043d\u043e\u0433\u043e\u0440\u0438\u044f", "MNE");
        CountryNormalizer.addCountry("kosovo", "\u043a\u043e\u0441\u043e\u0432\u043e", "KOS");
        CountryNormalizer.addCountry("belarus", "\u0431\u0435\u043b\u0430\u0440\u0443\u0441\u044c", "BLR");
        MAPPINGS.put("\u0431\u0435\u043b\u043e\u0440\u0443\u0441\u0441\u0438\u044f", MAPPINGS.get("belarus"));
        CountryNormalizer.addCountry("ukraine", "\u0443\u043a\u0440\u0430\u0438\u043d\u0430", "UKR");
        CountryNormalizer.addCountry("estonia", "\u044d\u0441\u0442\u043e\u043d\u0438\u044f", "EST");
        CountryNormalizer.addCountry("latvia", "\u043b\u0430\u0442\u0432\u0438\u044f", "LAT");
        CountryNormalizer.addCountry("lithuania", "\u043b\u0438\u0442\u0432\u0430", "LTU");
        CountryNormalizer.addCountry("moldova", "\u043c\u043e\u043b\u0434\u043e\u0432\u0430", "MDA");
        MAPPINGS.put("\u043c\u043e\u043b\u0434\u0430\u0432\u0438\u044f", MAPPINGS.get("moldova"));
        CountryNormalizer.addCountry("armenia", "\u0430\u0440\u043c\u0435\u043d\u0438\u044f", "ARM");
        CountryNormalizer.addCountry("georgia", "\u0433\u0440\u0443\u0437\u0438\u044f", "GEO");
        CountryNormalizer.addCountry("azerbaijan", "\u0430\u0437\u0435\u0440\u0431\u0430\u0439\u0434\u0436\u0430\u043d", "AZE");
        CountryNormalizer.addCountry("kazakhstan", "\u043a\u0430\u0437\u0430\u0445\u0441\u0442\u0430\u043d", "KAZ");
        CountryNormalizer.addCountry("uzbekistan", "\u0443\u0437\u0431\u0435\u043a\u0438\u0441\u0442\u0430\u043d", "UZB");
        CountryNormalizer.addCountry("israel", "\u0438\u0437\u0440\u0430\u0438\u043b\u044c", "ISR");
        CountryNormalizer.addCountry("el-salvador", "\u0441\u0430\u043b\u044c\u0432\u0430\u0434\u043e\u0440", "SLV");
        MAPPINGS.put("\u044d\u043b\u044c-\u0441\u0430\u043b\u044c\u0432\u0430\u0434\u043e\u0440", MAPPINGS.get("el-salvador"));
        CountryNormalizer.addCountry("brazil", "\u0431\u0440\u0430\u0437\u0438\u043b\u0438\u044f", "BRA");
        MAPPINGS.put("brasil", MAPPINGS.get("brazil"));
        CountryNormalizer.addCountry("argentina", "\u0430\u0440\u0433\u0435\u043d\u0442\u0438\u043d\u0430", "ARG");
        CountryNormalizer.addCountry("usa", "\u0441\u0448\u0430", "USA");
        MAPPINGS.put("united-states", MAPPINGS.get("usa"));
        CountryNormalizer.addCountry("canada", "\u043a\u0430\u043d\u0430\u0434\u0430", "CAN");
        CountryNormalizer.addCountry("mexico", "\u043c\u0435\u043a\u0441\u0438\u043a\u0430", "MEX");
        CountryNormalizer.addCountry("colombia", "\u043a\u043e\u043b\u0443\u043c\u0431\u0438\u044f", "COL");
        CountryNormalizer.addCountry("chile", "\u0447\u0438\u043b\u0438", "CHI");
        CountryNormalizer.addCountry("peru", "\u043f\u0435\u0440\u0443", "PER");
        CountryNormalizer.addCountry("uruguay", "\u0443\u0440\u0443\u0433\u0432\u0430\u0439", "URU");
        CountryNormalizer.addCountry("paraguay", "\u043f\u0430\u0440\u0430\u0433\u0432\u0430\u0439", "PAR");
        CountryNormalizer.addCountry("ecuador", "\u044d\u043a\u0432\u0430\u0434\u043e\u0440", "ECU");
        CountryNormalizer.addCountry("bolivia", "\u0431\u043e\u043b\u0438\u0432\u0438\u044f", "BOL");
        CountryNormalizer.addCountry("venezuela", "\u0432\u0435\u043d\u0435\u0441\u0443\u044d\u043b\u0430", "VEN");
        CountryNormalizer.addCountry("costa-rica", "\u043a\u043e\u0441\u0442\u0430-\u0440\u0438\u043a\u0430", "CRC");
        CountryNormalizer.addCountry("guatemala", "\u0433\u0432\u0430\u0442\u0435\u043c\u0430\u043b\u0430", "GUA");
        CountryNormalizer.addCountry("honduras", "\u0433\u043e\u043d\u0434\u0443\u0440\u0430\u0441", "HON");
        CountryNormalizer.addCountry("nicaragua", "\u043d\u0438\u043a\u0430\u0440\u0430\u0433\u0443\u0430", "NCA");
        CountryNormalizer.addCountry("panama", "\u043f\u0430\u043d\u0430\u043c\u0430", "PAN");
        CountryNormalizer.addCountry("jamaica", "\u044f\u043c\u0430\u0439\u043a\u0430", "JAM");
        CountryNormalizer.addCountry("japan", "\u044f\u043f\u043e\u043d\u0438\u044f", "JPN");
        CountryNormalizer.addCountry("china", "\u043a\u0438\u0442\u0430\u0439", "CHN");
        CountryNormalizer.addCountry("south-korea", "\u044e\u0436\u043d\u0430\u044f \u043a\u043e\u0440\u0435\u044f", "KOR");
        MAPPINGS.put("\u043a\u043e\u0440\u0435\u044f", MAPPINGS.get("south-korea"));
        CountryNormalizer.addCountry("australia", "\u0430\u0432\u0441\u0442\u0440\u0430\u043b\u0438\u044f", "AUS");
        CountryNormalizer.addCountry("new-zealand", "\u043d\u043e\u0432\u0430\u044f \u0437\u0435\u043b\u0430\u043d\u0434\u0438\u044f", "NZL");
        CountryNormalizer.addCountry("india", "\u0438\u043d\u0434\u0438\u044f", "IND");
        CountryNormalizer.addCountry("thailand", "\u0442\u0430\u0438\u043b\u0430\u043d\u0434", "THA");
        CountryNormalizer.addCountry("vietnam", "\u0432\u044c\u0435\u0442\u043d\u0430\u043c", "VIE");
        CountryNormalizer.addCountry("indonesia", "\u0438\u043d\u0434\u043e\u043d\u0435\u0437\u0438\u044f", "IDN");
        CountryNormalizer.addCountry("malaysia", "\u043c\u0430\u043b\u0430\u0439\u0437\u0438\u044f", "MAS");
        CountryNormalizer.addCountry("singapore", "\u0441\u0438\u043d\u0433\u0430\u043f\u0443\u0440", "SGP");
        CountryNormalizer.addCountry("philippines", "\u0444\u0438\u043b\u0438\u043f\u043f\u0438\u043d\u044b", "PHI");
        CountryNormalizer.addCountry("saudi-arabia", "\u0441\u0430\u0443\u0434\u043e\u0432\u0441\u043a\u0430\u044f \u0430\u0440\u0430\u0432\u0438\u044f", "KSA");
        CountryNormalizer.addCountry("uae", "\u043e\u0430\u044d", "UAE");
        CountryNormalizer.addCountry("qatar", "\u043a\u0430\u0442\u0430\u0440", "QAT");
        CountryNormalizer.addCountry("iran", "\u0438\u0440\u0430\u043d", "IRN");
        CountryNormalizer.addCountry("south-africa", "\u044e\u0430\u0440", "RSA");
        CountryNormalizer.addCountry("egypt", "\u0435\u0433\u0438\u043f\u0435\u0442", "EGY");
        CountryNormalizer.addCountry("morocco", "\u043c\u0430\u0440\u043e\u043a\u043a\u043e", "MAR");
        CountryNormalizer.addCountry("tunisia", "\u0442\u0443\u043d\u0438\u0441", "TUN");
        CountryNormalizer.addCountry("algeria", "\u0430\u043b\u0436\u0438\u0440", "ALG");
        CountryNormalizer.addCountry("nigeria", "\u043d\u0438\u0433\u0435\u0440\u0438\u044f", "NGA");
        CountryNormalizer.addCountry("ghana", "\u0433\u0430\u043d\u043d\u0430", "GHA");
        CountryNormalizer.addCountry("senegal", "\u0441\u0435\u043d\u0435\u0433\u0430\u043b", "SEN");
        CountryNormalizer.addCountry("cameroon", "\u043a\u0430\u043c\u0435\u0440\u0443\u043d", "CMR");
        CountryNormalizer.addCountry("ivory-coast", "\u043a\u043e\u0442-\u0434'\u0438\u0432\u0443\u0430\u0440", "CIV");
        CountryNormalizer.addCountry("kenya", "\u043a\u0435\u043d\u0438\u044f", "KEN");
    }

    public record CountryInfo(String slug, String code, boolean isInternational) {
    }
}

