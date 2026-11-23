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
        addCountry("portugal", "португалия", "POR", "португалии", "португальский", "португальская");
        addCountry("england", "англия", "ENG", "англии", "английский", "английская");
        addCountry("great-britain", "великобритания", "GBR", "великобритании");
        addCountry("scotland", "шотландия", "SCO", "шотландии", "шотландский", "шотландская");
        addCountry("wales", "уэльс", "WAL", "уэльса");
        addCountry("northern-ireland", "северная ирландия", "NIR", "северной ирландии");
        addCountry("ireland", "ирландия", "IRL", "ирландии", "ирландский", "ирландская");
        addCountry("spain", "испания", "ESP", "испании", "испанский", "испанская");
        addCountry("russia", "россия", "RUS", "россии", "российский", "российская");
        MAPPINGS.put("рф", MAPPINGS.get("russia"));
        addCountry("germany", "германия", "GER", "германии", "немецкий", "немецкая");
        addCountry("italy", "италия", "ITA", "италии", "итальянский", "итальянская", "итальянское");
        addCountry("france", "франция", "FRA", "франции", "французский", "французская");
        addCountry("netherlands", "нидерланды", "NED", "нидерландов", "голландский", "голландия", "holland");
        addCountry("belgium", "бельгия", "BEL", "бельгии", "бельгийский", "бельгийская");
        addCountry("switzerland", "швейцария", "SUI", "швейцарии", "швейцарский", "швейцарская");
        addCountry("austria", "австрия", "AUT", "австрии", "австрийский", "австрийская");
        addCountry("sweden", "швеция", "SWE", "швеции", "шведский", "шведская");
        addCountry("norway", "норвегия", "NOR", "норвегии", "норвежский", "норвежская");
        addCountry("denmark", "дания", "DEN", "дании", "датский", "датская");
        addCountry("finland", "финляндия", "FIN", "финляндии", "финский", "финская");
        addCountry("iceland", "исландия", "ISL", "исландии", "исландский");
        addCountry("poland", "польша", "POL", "польши", "польский", "польская");
        addCountry("czech-republic", "чехия", "CZE", "чехии", "чешский", "чешская", "czechia");
        addCountry("slovakia", "словакия", "SVK", "словакии", "словацкий", "словацкая");
        addCountry("hungary", "венгрия", "HUN", "венгрии", "венгерский", "венгерская");
        addCountry("romania", "румыния", "ROU", "румынии", "румынский", "румынская");
        addCountry("bulgaria", "болгария", "BUL", "болгарии", "болгарский", "болгарская");
        addCountry("greece", "греция", "GRE", "греции", "греческий", "греческая");
        addCountry("turkey", "турция", "TUR", "турции", "турецкий", "турецкая", "türkiye");
        addCountry("cyprus", "кипр", "CYP", "кипра", "кипрский");
        addCountry("serbia", "сербия", "SRB", "сербии", "сербский", "сербская");
        addCountry("croatia", "хорватия", "CRO", "хорватии", "хорватский", "хорватская");
        addCountry("slovenia", "словения", "SVN", "словении", "словенский", "словенская");
        addCountry("bosnia-and-herzegovina", "босния и герцеговина", "BIH", "боснии", "босния", "боснийский", "bosnia");
        addCountry("albania", "албания", "ALB", "албании", "албанский");
        addCountry("north-macedonia", "северная македония", "MKD", "македонии", "северной македонии", "македония", "macedonia");
        addCountry("montenegro", "черногория", "MNE", "черногории", "черногорский");
        addCountry("kosovo", "косово", "KOS");
        addCountry("belarus", "беларусь", "BLR", "белоруссия", "беларуси", "белоруссии", "белорусский", "белорусская");
        addCountry("ukraine", "украина", "UKR", "украины", "украинский", "украинская");
        addCountry("estonia", "эстония", "EST", "эстонии", "эстонский");
        addCountry("latvia", "латвия", "LAT", "латвии", "латвийский");
        addCountry("lithuania", "литва", "LTU", "литвы", "литовский", "литовская");
        addCountry("moldova", "молдова", "MDA", "молдавия", "молдовы", "молдавии", "молдавский");
        addCountry("armenia", "армения", "ARM", "армении", "армянский", "армянская");
        addCountry("georgia", "грузия", "GEO", "грузии", "грузинский");
        addCountry("azerbaijan", "азербайджан", "AZE", "азербайджана", "азербайджанский");
        addCountry("kazakhstan", "казахстан", "KAZ", "казахстана", "казахский", "казахская");
        addCountry("uzbekistan", "узбекистан", "UZB", "узбекистана", "узбекский");
        addCountry("israel", "израиль", "ISR", "израиля", "израильский");

        // --- AMERICAS ---
        addCountry("el-salvador", "сальвадор", "SLV", "эль-сальвадор");
        addCountry("brazil", "бразилия", "BRA", "бразилии", "бразильский", "бразильская", "brasil");
        addCountry("argentina", "аргентина", "ARG", "аргентины", "аргентинский", "аргентинская");
        addCountry("usa", "сша", "USA", "united-states", "америка");
        addCountry("canada", "канада", "CAN", "канады", "канадский");
        addCountry("mexico", "мексика", "MEX", "мексики", "мексиканский", "мексиканская");
        addCountry("colombia", "колумбия", "COL", "колумбии", "колумбийский");
        addCountry("chile", "чили", "CHI");
        addCountry("peru", "перу", "PER");
        addCountry("uruguay", "уругвай", "URU", "уругвая");
        addCountry("paraguay", "парагвай", "PAR", "парагвая");
        addCountry("ecuador", "эквадор", "ECU", "эквадора");
        addCountry("bolivia", "боливия", "BOL", "боливии");
        addCountry("venezuela", "венесуэла", "VEN", "венесуэлы");
        addCountry("costa-rica", "коста-рика", "CRC");
        addCountry("guatemala", "гватемала", "GUA");
        addCountry("honduras", "гондурас", "HON");
        addCountry("nicaragua", "никарагуа", "NCA");
        addCountry("panama", "панама", "PAN", "панамы");
        addCountry("jamaica", "ямайка", "JAM", "ямайки");

        // --- ASIA & OCEANIA ---
        addCountry("japan", "япония", "JPN", "японии", "японский", "японская");
        addCountry("china", "китай", "CHN", "китая", "китайский", "китайская");
        addCountry("south-korea", "южная корея", "KOR", "кореи", "южной кореи", "корея");
        addCountry("australia", "австралия", "AUS", "австралии", "австралийский", "австралийская");
        addCountry("new-zealand", "новая зеландия", "NZL", "новой зеландии", "новозеландский");
        addCountry("india", "индия", "IND", "индии", "индийский");
        addCountry("thailand", "таиланд", "THA", "таиланда", "тайский");
        addCountry("vietnam", "вьетнам", "VIE", "вьетнама");
        addCountry("indonesia", "индонезия", "IDN", "индонезии", "индонезийский");
        addCountry("malaysia", "малайзия", "MAS", "малайзии");
        addCountry("singapore", "сингапур", "SGP", "сингапура");
        addCountry("philippines", "филиппины", "PHI", "филиппин");
        addCountry("saudi-arabia", "саудовская аравия", "KSA", "саудовской аравии");
        addCountry("uae", "оаэ", "UAE");
        addCountry("qatar", "катар", "QAT", "катара");
        addCountry("iran", "иран", "IRN", "ирана");

        // --- AFRICA ---
        addCountry("south-africa", "юар", "RSA");
        addCountry("egypt", "египет", "EGY", "египта", "египетский");
        addCountry("morocco", "марокко", "MAR");
        addCountry("tunisia", "тунис", "TUN", "туниса");
        addCountry("algeria", "алжир", "ALG", "алжира");
        addCountry("nigeria", "нигерия", "NGA", "нигерии");
        addCountry("ghana", "ганна", "GHA", "гана", "ганы");
        addCountry("senegal", "сенегал", "SEN", "сенегала");
        addCountry("cameroon", "камерун", "CMR", "камеруна");
        addCountry("ivory-coast", "кот-д'ивуар", "CIV");
        addCountry("kenya", "кения", "KEN", "кении");
    }

    private static void addCountry(String slug, String ruName, String code, String... aliases) {
        CountryInfo info = new CountryInfo(slug, code, false);
        MAPPINGS.put(slug, info);
        MAPPINGS.put(ruName, info);
        if (aliases != null) {
            for (String alias : aliases) {
                if (alias != null && !alias.isBlank()) {
                    MAPPINGS.put(alias.trim().toLowerCase(), info);
                }
            }
        }
    }

    public static CountryInfo extractCountryFromTournament(String tournamentName) {
        if (tournamentName == null || tournamentName.isBlank()) return null;
        String lower = tournamentName.toLowerCase();

        // 1. Check multi-word country names first
        for (Map.Entry<String, CountryInfo> entry : MAPPINGS.entrySet()) {
            String key = entry.getKey();
            if (key.contains(" ") || key.contains("-")) {
                if (lower.contains(key)) {
                    return entry.getValue();
                }
            }
        }

        // 2. Tokenize and check single words
        String[] tokens = lower.split("[^a-zA-Zа-яА-Я0-9]+");
        for (String token : tokens) {
            if (token.length() >= 4 && !INVALID_COUNTRY_WORDS.contains(token)) {
                CountryInfo info = MAPPINGS.get(token);
                if (info != null) {
                    return info;
                }
            }
        }
        return null;
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
