package pro.datawiki.igaming.mdm.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.CanonicalMatchHeaderDto;
import pro.datawiki.igaming.dto.CanonicalTeamDto;
import pro.datawiki.igaming.dto.util.SimilarityUtils;
import pro.datawiki.igaming.mdm.domain.*;
import pro.datawiki.igaming.mdm.repository.MdmTeamTranslationRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentTranslationRepository;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Universal polymorphic service for multilingual resolution and normalization of sports team
 * and tournament names across MDM feeds, APIs, Portal Gateway, and AI Analyst pipelines.
 *
 * Implements strict Anti-Cyrillic Guard for Latin locales (fr, en, es, de, it, pt) and
 * graceful fallback hierarchies: requested_lang -> en -> canonical -> deterministic transliteration.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TeamNameLocalizationService {

    private final MdmTeamTranslationRepository teamTranslationRepository;
    private final MdmTournamentTranslationRepository tournamentTranslationRepository;

    // High performance short-lived cache for team and tournament translations (10 min TTL)
    private final Cache<Long, Map<String, String>> teamTranslationsCache = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    private final Cache<Long, Map<String, String>> tournamentTranslationsCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    private static final Set<String> LATIN_LOCALES = Set.of("en", "fr", "es", "de", "it", "pt", "nl", "tr", "pl", "ro");

    /**
     * Resolves the best localized display name for a team.
     *
     * @param team       MDM team entity
     * @param lang       Target language code (e.g. "fr", "en", "ru", "es")
     * @param teamTrans  Known translations map for this team (lang -> name)
     * @return Clean, human-readable display name suitable for the target locale
     */
    public String resolveTeamName(MdmTeam team, String lang, Map<String, String> teamTrans) {
        if (team == null) {
            return "Unknown Team";
        }
        String canonical = team.getCanonicalName() != null ? team.getCanonicalName().trim() : "";
        String normalizedLang = normalizeLangCode(lang);
        Map<String, String> trans = teamTrans != null ? teamTrans : getTranslationsForTeam(team.getId());

        // 1. Direct translation for target language
        if (trans != null && !trans.isEmpty()) {
            String exact = trans.get(normalizedLang);
            if (isValidName(exact)) {
                // If Latin locale requested, ensure it doesn't contain Cyrillic
                if (!isLatinLocale(normalizedLang) || !SimilarityUtils.hasCyrillic(exact)) {
                    return formatDisplayName(exact);
                }
            }
        }

        // 2. Fallback for Russian locale
        if ("ru".equals(normalizedLang)) {
            if (trans != null && isValidName(trans.get("ru"))) {
                return formatDisplayName(trans.get("ru"));
            }
            if (SimilarityUtils.hasCyrillic(canonical)) {
                return formatDisplayName(canonical);
            }
            // If canonical is Latin (e.g. Chelsea, Real Madrid), return it cleanly
            return formatDisplayName(canonical);
        }

        // 3. Fallback for Latin / European locales (fr, en, es, de, it, pt, etc.)
        // Priority: en translation -> canonical (if non-cyrillic) -> transliteration of Cyrillic
        if (trans != null) {
            String enTrans = trans.get("en");
            if (isValidName(enTrans) && !SimilarityUtils.hasCyrillic(enTrans)) {
                return formatDisplayName(enTrans);
            }
        }

        if (isValidName(canonical) && !SimilarityUtils.hasCyrillic(canonical)) {
            return formatDisplayName(canonical);
        }

        // 4. Strict Latin Guard: Transliterate Cyrillic into clean Latin
        if (SimilarityUtils.hasCyrillic(canonical)) {
            String transliterated = SimilarityUtils.transliterateCyrillic(canonical);
            return formatDisplayName(transliterated);
        }

        return formatDisplayName(canonical.isEmpty() ? "Team #" + team.getId() : canonical);
    }

    /**
     * Resolves the best localized tournament name.
     */
    public String resolveTournamentName(MdmTournament tournament, String lang, Map<String, String> tournTrans) {
        if (tournament == null) {
            return "Tournament";
        }
        String canonical = tournament.getCanonicalName() != null ? tournament.getCanonicalName().trim() : "";
        String normalizedLang = normalizeLangCode(lang);
        Map<String, String> trans = tournTrans != null ? tournTrans : getTranslationsForTournament(tournament.getId());

        if (trans != null && isValidName(trans.get(normalizedLang))) {
            return formatDisplayName(trans.get(normalizedLang));
        }

        if ("ru".equals(normalizedLang)) {
            if (trans != null && isValidName(trans.get("ru"))) {
                return formatDisplayName(trans.get("ru"));
            }
            return formatDisplayName(canonical);
        }

        // Latin locales: prefer English translation or Latin canonical
        if (trans != null && isValidName(trans.get("en")) && !SimilarityUtils.hasCyrillic(trans.get("en"))) {
            return formatDisplayName(trans.get("en"));
        }

        if (isValidName(canonical) && !SimilarityUtils.hasCyrillic(canonical)) {
            return formatDisplayName(canonical);
        }

        if (SimilarityUtils.hasCyrillic(canonical)) {
            return formatDisplayName(SimilarityUtils.transliterateCyrillic(canonical));
        }

        return formatDisplayName(canonical);
    }

    /**
     * Batch loads all translations for given matches to minimize DB roundtrips.
     */
    public Map<Long, Map<String, String>> loadTeamTranslations(Collection<MdmMatch> matches) {
        if (matches == null || matches.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> teamIds = new HashSet<>();
        for (MdmMatch m : matches) {
            if (m.getHomeTeam() != null && m.getHomeTeam().getId() != null) teamIds.add(m.getHomeTeam().getId());
            if (m.getAwayTeam() != null && m.getAwayTeam().getId() != null) teamIds.add(m.getAwayTeam().getId());
        }
        return loadTeamTranslationsByIds(teamIds);
    }

    public Map<Long, Map<String, String>> loadTeamTranslationsByIds(Collection<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Map<String, String>> result = new HashMap<>();
        Set<Long> missingIds = new HashSet<>();

        for (Long id : teamIds) {
            Map<String, String> cached = teamTranslationsCache.getIfPresent(id);
            if (cached != null) {
                result.put(id, cached);
            } else {
                missingIds.add(id);
            }
        }

        if (!missingIds.isEmpty()) {
            List<MdmTeamTranslation> rows = teamTranslationRepository.findAllByTeamIdIn(missingIds);
            Map<Long, Map<String, String>> fetched = new HashMap<>();
            for (MdmTeamTranslation row : rows) {
                if (row.getTeam() != null && row.getTeam().getId() != null && row.getLang() != null && row.getName() != null) {
                    fetched.computeIfAbsent(row.getTeam().getId(), k -> new ConcurrentHashMap<>())
                            .put(row.getLang().toLowerCase().trim(), row.getName());
                }
            }

            for (Long id : missingIds) {
                Map<String, String> transMap = fetched.getOrDefault(id, Collections.emptyMap());
                teamTranslationsCache.put(id, transMap);
                result.put(id, transMap);
            }
        }

        return result;
    }

    public Map<Long, Map<String, String>> loadTournamentTranslations(Collection<MdmMatch> matches) {
        if (matches == null || matches.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> tournIds = matches.stream()
                .map(MdmMatch::getTournament)
                .filter(Objects::nonNull)
                .map(MdmTournament::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (tournIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Map<String, String>> result = new HashMap<>();
        Set<Long> missingIds = new HashSet<>();

        for (Long id : tournIds) {
            Map<String, String> cached = tournamentTranslationsCache.getIfPresent(id);
            if (cached != null) {
                result.put(id, cached);
            } else {
                missingIds.add(id);
            }
        }

        if (!missingIds.isEmpty()) {
            List<MdmTournamentTranslation> rows = tournamentTranslationRepository.findAllByTournamentIdIn(missingIds);
            Map<Long, Map<String, String>> fetched = new HashMap<>();
            for (MdmTournamentTranslation row : rows) {
                if (row.getTournament() != null && row.getTournament().getId() != null && row.getLang() != null && row.getName() != null) {
                    fetched.computeIfAbsent(row.getTournament().getId(), k -> new ConcurrentHashMap<>())
                            .put(row.getLang().toLowerCase().trim(), row.getName());
                }
            }

            for (Long id : missingIds) {
                Map<String, String> transMap = fetched.getOrDefault(id, Collections.emptyMap());
                tournamentTranslationsCache.put(id, transMap);
                result.put(id, transMap);
            }
        }

        return result;
    }

    public Map<String, String> getTranslationsForTeam(Long teamId) {
        if (teamId == null) return Collections.emptyMap();
        Map<Long, Map<String, String>> map = loadTeamTranslationsByIds(List.of(teamId));
        return map.getOrDefault(teamId, Collections.emptyMap());
    }

    public Map<String, String> getTranslationsForTournament(Long tournId) {
        if (tournId == null) return Collections.emptyMap();
        Map<String, String> cached = tournamentTranslationsCache.getIfPresent(tournId);
        if (cached != null) return cached;
        List<MdmTournamentTranslation> rows = tournamentTranslationRepository.findByTournamentId(tournId);
        Map<String, String> map = new ConcurrentHashMap<>();
        for (MdmTournamentTranslation row : rows) {
            if (row.getLang() != null && row.getName() != null) {
                map.put(row.getLang().toLowerCase().trim(), row.getName());
            }
        }
        tournamentTranslationsCache.put(tournId, map);
        return map;
    }

    /**
     * Converts an MdmMatch to a localized CanonicalMatchHeaderDto according to the specified language.
     */
    public CanonicalMatchHeaderDto toLocalizedHeaderDto(
            MdmMatch match,
            String lang,
            Map<Long, Map<String, String>> teamTranslations,
            Map<Long, Map<String, String>> tournTranslations
    ) {
        if (match == null) return null;

        CanonicalMatchHeaderDto header = MdmMatchResolutionService.toHeaderDto(match);

        MdmTeam ht = match.getHomeTeam();
        MdmTeam at = match.getAwayTeam();
        MdmTournament t = match.getTournament();

        Map<String, String> htTrans = (ht != null && teamTranslations != null) ? teamTranslations.get(ht.getId()) : null;
        Map<String, String> atTrans = (at != null && teamTranslations != null) ? teamTranslations.get(at.getId()) : null;
        Map<String, String> tTrans = (t != null && tournTranslations != null) ? tournTranslations.get(t.getId()) : null;

        String localizedHome = resolveTeamName(ht, lang, htTrans);
        String localizedAway = resolveTeamName(at, lang, atTrans);
        String localizedTourn = resolveTournamentName(t, lang, tTrans);

        // Populate both primary name (for direct backward-compatible consumers) and explicit localized fields
        if (isValidName(localizedHome)) {
            header.setHomeTeamName(localizedHome);
            header.setHomeTeamLocalizedName(localizedHome);
        }
        if (isValidName(localizedAway)) {
            header.setAwayTeamName(localizedAway);
            header.setAwayTeamLocalizedName(localizedAway);
        }
        if (isValidName(localizedTourn)) {
            header.setTournamentName(localizedTourn);
            header.setTournamentLocalizedName(localizedTourn);
        }

        if (htTrans != null && !htTrans.isEmpty()) {
            header.setHomeTeamTranslations(new HashMap<>(htTrans));
        }
        if (atTrans != null && !atTrans.isEmpty()) {
            header.setAwayTeamTranslations(new HashMap<>(atTrans));
        }

        return header;
    }

    /**
     * Converts an MdmTeam to a localized CanonicalTeamDto.
     */
    public CanonicalTeamDto toLocalizedTeamDto(MdmTeam team, String lang) {
        if (team == null) return null;
        Map<String, String> trans = getTranslationsForTeam(team.getId());
        String localized = resolveTeamName(team, lang, trans);

        return CanonicalTeamDto.builder()
                .teamId(team.getId())
                .sportId(team.getSport() != null ? team.getSport().getId() : null)
                .sportSlug(team.getSport() != null ? team.getSport().getSlug() : null)
                .categoryId(team.getCategory() != null ? team.getCategory().getId() : null)
                .categorySlug(team.getCategory() != null ? team.getCategory().getSlug() : null)
                .countryId(team.getCountry() != null ? team.getCountry().getId() : null)
                .countryName(team.getCountry() != null ? team.getCountry().getCanonicalName() : team.getCountryName())
                .canonicalName(team.getCanonicalName())
                .localizedName(localized)
                .translations(trans.isEmpty() ? null : new HashMap<>(trans))
                .slug(team.getSlug())
                .ageCategory(team.getAgeCategory())
                .gender(team.getGender())
                .sofascoreId(team.getSofascoreId())
                .sourceOrigin(team.getSourceOrigin())
                .logoUrl(team.getLogoUrl())
                .build();
    }

    public static String normalizeLangCode(String lang) {
        if (lang == null || lang.isBlank()) {
            return "en";
        }
        String clean = lang.trim().toLowerCase();
        int dashIdx = clean.indexOf('-');
        if (dashIdx > 0) {
            clean = clean.substring(0, dashIdx);
        }
        int underIdx = clean.indexOf('_');
        if (underIdx > 0) {
            clean = clean.substring(0, underIdx);
        }
        return clean;
    }

    private static boolean isLatinLocale(String lang) {
        return LATIN_LOCALES.contains(lang);
    }

    private static boolean isValidName(String str) {
        return str != null && !str.isBlank();
    }

    private static String formatDisplayName(String str) {
        if (str == null || str.isBlank()) return "";
        String[] words = str.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (w.equalsIgnoreCase("fc") || w.equalsIgnoreCase("фк")
                    || w.equalsIgnoreCase("bc") || w.equalsIgnoreCase("бк")
                    || w.equalsIgnoreCase("hc") || w.equalsIgnoreCase("хк")
                    || w.equalsIgnoreCase("sc") || w.equalsIgnoreCase("ск")
                    || w.equalsIgnoreCase("u17") || w.equalsIgnoreCase("u18")
                    || w.equalsIgnoreCase("u19") || w.equalsIgnoreCase("u21")
                    || w.equalsIgnoreCase("u23") || w.equalsIgnoreCase("cska")
                    || w.equalsIgnoreCase("цска") || w.equalsIgnoreCase("ska")
                    || w.equalsIgnoreCase("ска") || w.equalsIgnoreCase("psg")) {
                sb.append(w.toUpperCase()).append(" ");
            } else if (Character.isUpperCase(w.charAt(0)) && w.substring(1).equals(w.substring(1).toLowerCase())) {
                sb.append(w).append(" ");
            } else {
                sb.append(Character.toUpperCase(w.charAt(0)))
                        .append(w.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
