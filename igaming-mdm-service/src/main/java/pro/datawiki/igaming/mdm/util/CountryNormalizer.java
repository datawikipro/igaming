package pro.datawiki.igaming.mdm.util;

import pro.datawiki.igaming.mdm.service.MdmMatchResolutionService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CountryNormalizer {

    public record CountryInfo(String slug, String code, boolean isInternational) {}

    private static final Map<String, CountryInfo> MAPPINGS = new HashMap<>();

    private static final Set<String> INVALID_COUNTRY_WORDS = Set.of(
            "футбол", "киберфутбол", "теннис", "хоккей", "баскетбол", "кибербаскетбол",
            "киберспорт", "волейбол", "гандбол", "бейсбол", "регби", "снукер", "дартс",
            "football", "soccer", "esports", "tennis", "hockey", "basketball", "volleyball",
            "лига", "кубок", "дивизион", "чемпионат", "турнир", "первенство", "серия",
            "league", "cup", "division", "championship", "tournament",
            "мужчины", "женщины", "мол", "молодежная", "резерв", "статистика",
            "men", "women", "youth", "reserve", "statistics", "stats",
            "1-й дивизион", "2-й дивизион", "1-я лига", "2-я лига", "чемпионат 1", "чемпионат 2"
    );

    static {
        // International
        CountryInfo intl = new CountryInfo("international", "INT", true);
        MAPPINGS.put("international", intl);
        MAPPINGS.put("world", intl);
        MAPPINGS.put("мир", intl);
        MAPPINGS.put("международные", intl);
        MAPPINGS.put("международный", intl);
        MAPPINGS.put("еврокубки", intl);
        MAPPINGS.put("europe", intl);
        MAPPINGS.put("европа", intl);
        MAPPINGS.put("интернешнл", intl);
        MAPPINGS.put("глобальный", intl);

        // --- EUROPE ---
        addCountry("portugal", "португалия", "POR");
        addCountry("england", "англия", "ENG");
        addCountry("great-britain", "великобритания", "GBR");
        addCountry("scotland", "шотландия", "SCO");
        addCountry("wales", "уэльс", "WAL");
        addCountry("northern-ireland", "северная ирландия", "NIR");
        addCountry("ireland", "ирландия", "IRL");
        addCountry("spain", "испания", "ESP");
        addCountry("russia", "россия", "RUS");
        MAPPINGS.put("рф", MAPPINGS.get("russia"));
        addCountry("germany", "германия", "GER");
        addCountry("italy", "италия", "ITA");
        addCountry("france", "франция", "FRA");
        addCountry("netherlands", "нидерланды", "NED");
        MAPPINGS.put("голландия", MAPPINGS.get("netherlands"));
        MAPPINGS.put("holland", MAPPINGS.get("netherlands"));
        addCountry("belgium", "бельгия", "BEL");
        addCountry("switzerland", "швейцария", "SUI");
        addCountry("austria", "австрия", "AUT");
        addCountry("sweden", "швеция", "SWE");
        addCountry("norway", "норвегия", "NOR");
        addCountry("denmark", "дания", "DEN");
        addCountry("finland", "финляндия", "FIN");
        addCountry("iceland", "исландия", "ISL");
        addCountry("poland", "польша", "POL");
        addCountry("czech-republic", "чехия", "CZE");
        MAPPINGS.put("czechia", MAPPINGS.get("czech-republic"));
        addCountry("slovakia", "словакия", "SVK");
        addCountry("hungary", "венгрия", "HUN");
        addCountry("romania", "румыния", "ROU");
        addCountry("bulgaria", "болгария", "BUL");
        addCountry("greece", "греция", "GRE");
        addCountry("turkey", "турция", "TUR");
        MAPPINGS.put("türkiye", MAPPINGS.get("turkey"));
        addCountry("cyprus", "кипр", "CYP");
        addCountry("serbia", "сербия", "SRB");
        addCountry("croatia", "хорватия", "CRO");
        addCountry("slovenia", "словения", "SVN");
        addCountry("bosnia-and-herzegovina", "босния и герцеговина", "BIH");
        MAPPINGS.put("босния", MAPPINGS.get("bosnia-and-herzegovina"));
        MAPPINGS.put("bosnia", MAPPINGS.get("bosnia-and-herzegovina"));
        addCountry("albania", "албания", "ALB");
        addCountry("north-macedonia", "северная македония", "MKD");
        MAPPINGS.put("македония", MAPPINGS.get("north-macedonia"));
        MAPPINGS.put("macedonia", MAPPINGS.get("north-macedonia"));
        addCountry("montenegro", "черногория", "MNE");
        addCountry("kosovo", "косово", "KOS");
        addCountry("belarus", "беларусь", "BLR");
        MAPPINGS.put("белоруссия", MAPPINGS.get("belarus"));
        addCountry("ukraine", "украина", "UKR");
        addCountry("estonia", "эстония", "EST");
        addCountry("latvia", "латвия", "LAT");
        addCountry("lithuania", "литва", "LTU");
        addCountry("moldova", "молдова", "MDA");
        MAPPINGS.put("молдавия", MAPPINGS.get("moldova"));
        addCountry("armenia", "армения", "ARM");
        addCountry("georgia", "грузия", "GEO");
        addCountry("azerbaijan", "азербайджан", "AZE");
        addCountry("kazakhstan", "казахстан", "KAZ");
        addCountry("uzbekistan", "узбекистан", "UZB");
        addCountry("israel", "израиль", "ISR");

        // --- AMERICAS ---
        addCountry("el-salvador", "сальвадор", "SLV");
        MAPPINGS.put("эль-сальвадор", MAPPINGS.get("el-salvador"));
        addCountry("brazil", "бразилия", "BRA");
        MAPPINGS.put("brasil", MAPPINGS.get("brazil"));
        addCountry("argentina", "аргентина", "ARG");
        addCountry("usa", "сша", "USA");
        MAPPINGS.put("united-states", MAPPINGS.get("usa"));
        addCountry("canada", "канада", "CAN");
        addCountry("mexico", "мексика", "MEX");
        addCountry("colombia", "колумбия", "COL");
        addCountry("chile", "чили", "CHI");
        addCountry("peru", "перу", "PER");
        addCountry("uruguay", "уругвай", "URU");
        addCountry("paraguay", "парагвай", "PAR");
        addCountry("ecuador", "эквадор", "ECU");
        addCountry("bolivia", "боливия", "BOL");
        addCountry("venezuela", "венесуэла", "VEN");
        addCountry("costa-rica", "коста-рика", "CRC");
        addCountry("guatemala", "гватемала", "GUA");
        addCountry("honduras", "гондурас", "HON");
        addCountry("nicaragua", "никарагуа", "NCA");
        addCountry("panama", "панама", "PAN");
        addCountry("jamaica", "ямайка", "JAM");

        // --- ASIA & OCEANIA ---
        addCountry("japan", "япония", "JPN");
        addCountry("china", "китай", "CHN");
        addCountry("south-korea", "южная корея", "KOR");
        MAPPINGS.put("корея", MAPPINGS.get("south-korea"));
        addCountry("australia", "австралия", "AUS");
        addCountry("new-zealand", "новая зеландия", "NZL");
        addCountry("india", "индия", "IND");
        addCountry("thailand", "таиланд", "THA");
        addCountry("vietnam", "вьетнам", "VIE");
        addCountry("indonesia", "индонезия", "IDN");
        addCountry("malaysia", "малайзия", "MAS");
        addCountry("singapore", "сингапур", "SGP");
        addCountry("philippines", "филиппины", "PHI");
        addCountry("saudi-arabia", "саудовская аравия", "KSA");
        addCountry("uae", "оаэ", "UAE");
        addCountry("qatar", "катар", "QAT");
        addCountry("iran", "иран", "IRN");

        // --- AFRICA ---
        addCountry("south-africa", "юар", "RSA");
        addCountry("egypt", "египет", "EGY");
        addCountry("morocco", "марокко", "MAR");
        addCountry("tunisia", "тунис", "TUN");
        addCountry("algeria", "алжир", "ALG");
        addCountry("nigeria", "нигерия", "NGA");
        addCountry("ghana", "ганна", "GHA");
        addCountry("senegal", "сенегал", "SEN");
        addCountry("cameroon", "камерун", "CMR");
        addCountry("ivory-coast", "кот-д'ивуар", "CIV");
        addCountry("kenya", "кения", "KEN");
    }

    private static void addCountry(String slug, String ruName, String code) {
        CountryInfo info = new CountryInfo(slug, code, false);
        MAPPINGS.put(slug, info);
        MAPPINGS.put(ruName, info);
    }

    private CountryNormalizer() {}

    public static CountryInfo normalize(String countryName, Boolean isInternational) {
        if (Boolean.TRUE.equals(isInternational)) {
            return MAPPINGS.get("international");
        }
        if (countryName == null || countryName.isBlank()) {
            return null;
        }
        String clean = countryName.trim().toLowerCase();

        // 1. Guard against sports, tournaments, or pseudo-country tokens
        if (INVALID_COUNTRY_WORDS.contains(clean)) {
            return null;
        }

        // 2. Exact match in comprehensive mappings
        CountryInfo exact = MAPPINGS.get(clean);
        if (exact != null) {
            return exact;
        }

        // 3. Slug match
        String slug = MdmMatchResolutionService.toSlug(clean);
        CountryInfo bySlug = MAPPINGS.get(slug);
        if (bySlug != null) {
            return bySlug;
        }

        // 4. Do not invent fake countries for arbitrary strings
        return null;
    }
}
