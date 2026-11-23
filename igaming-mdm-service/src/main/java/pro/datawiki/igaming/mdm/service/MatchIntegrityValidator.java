package pro.datawiki.igaming.mdm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.dto.MatchResolutionRequest;
import pro.datawiki.igaming.dto.signature.CyberMatchDetector;
import pro.datawiki.igaming.dto.sport.Sport;
import pro.datawiki.igaming.mdm.domain.MdmCategory;
import pro.datawiki.igaming.mdm.domain.MdmCountry;
import pro.datawiki.igaming.mdm.domain.MdmSport;
import pro.datawiki.igaming.mdm.domain.MdmTeam;
import pro.datawiki.igaming.mdm.domain.MdmTournament;
import pro.datawiki.igaming.mdm.repository.MdmTeamRepository;
import pro.datawiki.igaming.mdm.repository.MdmTournamentTeamRepository;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Service component responsible for validating MDM match, tournament and team integrity:
 * - Match-level homogeneity: gender, age category, doubles format (Mutant Match Guard)
 * - League-level integrity: country/category scoping, official roster membership, cyber isolation
 * - Input validation: synthetic placeholder detection, stats/outright props detection
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchIntegrityValidator {

    private final AnomalyReportingService anomalyReportingService;
    private final MdmTournamentTeamRepository tournamentTeamRepository;
    private final MdmTeamRepository teamRepository;

    private static final Pattern SYNTHETIC_LEAGUE_PATTERN = Pattern.compile(
            "(?ui)^\\s*league\\s*\\d+\\s*$|.*\\b(?:fantasy|mythical|мифическ(?:ий|ая|ое|ие)?|simulated\\s*reality|\\bsrl\\b).*"
    );

    private static final Pattern STATS_OR_OUTRIGHT_PATTERN = Pattern.compile(
            "(?ui).*(?:\\((?:угловые|фолы|желтые\\s*карт[а-я]*|броски\\s*в\\s*створ|двойные\\s*ошибки|эйсы|офсайды|удары\\s*от\\s*ворот|вбрасывания|штрафное\\s*время|удаления|подборы|передачи|блок[- ]?шоты|3[- ]?очковые)\\)|\\b(?:corners?|bookings?|fouls?|offsides?|shots\\s*on\\s*target|победитель(?:ница)?|pobeditel(?:nitsa)?|winner|обладатель\\s*кубка|выход\\s*в\\s*(?:финал|полуфинал|плей[- ]?офф)|чемпион|chempion|итоги|itogi|outright|futures)\\b).*"
    );

    private static final Pattern STAT_PREFIX_PATTERN = Pattern.compile(
            "^(?:угл|угловые|жк|ж\\.к\\.|фолы?|ауты?|офсайды?|броски?|удары?(?:\\s+в\\s+створ)?|штанги?(?:\\s+и\\s+перекладины?)?|штрафное\\s+время|вбрасывания?|corners?|bookings?|yellow\\s*cards?|fouls?|offsides?|shots?(?:\\s+on\\s+target)?)(?:[\\s\\.\\:\\-_]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final Pattern STAT_LEAGUE_PATTERN = Pattern.compile(
            "(?:[\\s\\.\\:_\\-\\(\\)]|^)(?:статистика|желтые\\s+карточки|угловые|фолы|офсайды|удары\\s+в\\s+створ|штанги\\s+и\\s+перекладины|штрафное\\s+время|броски\\s+в\\s+створ|вбрасывания|ауты|yellow\\s*cards?|corners?|fouls?|offsides?|shots?\\s+on\\s+target|penalty\\s+minutes?)(?:[\\s\\.\\:_\\-\\(\\)]|$)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    public void validateRequest(MatchResolutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("MatchResolutionRequest cannot be null");
        }
        if (isSyntheticPlaceholder(request.getHomeTeamName()) || isSyntheticPlaceholder(request.getAwayTeamName())) {
            throw new IllegalArgumentException("Synthetic placeholder match rejected: " + request.getHomeTeamName() + " vs " + request.getAwayTeamName());
        }
        if (isStatsOrOutrightMatch(request)) {
            throw new IllegalArgumentException("Stats props or outright pseudo-match rejected: " + request.getHomeTeamName() + " vs " + request.getAwayTeamName() + " in " + request.getTournamentName());
        }
        if (isMalformedDoubles(request)) {
            throw new IllegalArgumentException("Malformed doubles mismatch (singles vs doubles): '" + request.getHomeTeamName() + "' vs '" + request.getAwayTeamName() + "'");
        }
        if (request.getSportId() == null) {
            throw new IllegalArgumentException("Sport ID is mandatory");
        }
        if (request.getTournamentId() == null) {
            throw new IllegalArgumentException("Tournament ID is mandatory");
        }
        if (request.getHomeTeamId() == null || request.getAwayTeamId() == null) {
            throw new IllegalArgumentException("Home and Away Team IDs are mandatory");
        }
        if (Objects.equals(request.getHomeTeamId(), request.getAwayTeamId())) {
            throw new IllegalArgumentException("Home team and Away team cannot be identical (ID=" + request.getHomeTeamId() + ")");
        }
        if (request.getStartTimeEpochSec() == null || request.getStartTimeEpochSec() <= 0) {
            throw new IllegalArgumentException("Valid start time epoch is mandatory");
        }
    }

    public void validateLeagueIntegrity(MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String sourceOrigin, String createdBy) {
        // 0. Strict Match Homogeneity: Gender and Age Category (Mutant Match Guard)
        validateMatchGenderAndAge(tournament, homeTeam, awayTeam, createdBy);

        // 1. Gender check
        if (tournament.getGender() != null && !"MIXED".equalsIgnoreCase(tournament.getGender())) {
            if (homeTeam.getGender() != null && !Objects.equals(homeTeam.getGender(), tournament.getGender())) {
                String msg = String.format("Gender mismatch: Home team '%s' (%s) cannot play in %s tournament '%s'",
                        homeTeam.getCanonicalName(), homeTeam.getGender(), tournament.getGender(), tournament.getCanonicalName());
                if (anomalyReportingService != null) {
                    anomalyReportingService.reportLeagueMismatch(createdBy, "GENDER_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                }
                throw new IllegalArgumentException(msg);
            }
            if (awayTeam.getGender() != null && !Objects.equals(awayTeam.getGender(), tournament.getGender())) {
                String msg = String.format("Gender mismatch: Away team '%s' (%s) cannot play in %s tournament '%s'",
                        awayTeam.getCanonicalName(), awayTeam.getGender(), tournament.getGender(), tournament.getCanonicalName());
                if (anomalyReportingService != null) {
                    anomalyReportingService.reportLeagueMismatch(createdBy, "GENDER_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                }
                throw new IllegalArgumentException(msg);
            }
        }

        // 2. Age Category check
        boolean isCyber = (tournament.getSport() != null && (tournament.getSport().getId() == 37 || tournament.getSport().getId() == 6
                || tournament.getSport().getId() == 33 || tournament.getSport().getId() == 34 || tournament.getSport().getId() == 35))
                || CyberMatchDetector.isCyberLeague(tournament.getCanonicalName());

        if (tournament.getAgeCategory() != null && !isCyber) {
            if (homeTeam.getAgeCategory() != null && !isAgeCompatible(homeTeam.getAgeCategory(), tournament.getAgeCategory(), tournament)) {
                String msg = String.format("Age category mismatch: Home team '%s' (%s) cannot play in %s tournament '%s'",
                        homeTeam.getCanonicalName(), homeTeam.getAgeCategory(), tournament.getAgeCategory(), tournament.getCanonicalName());
                if (anomalyReportingService != null) {
                    anomalyReportingService.reportLeagueMismatch(createdBy, "AGE_CATEGORY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                }
                throw new IllegalArgumentException(msg);
            }
            if (awayTeam.getAgeCategory() != null && !isAgeCompatible(awayTeam.getAgeCategory(), tournament.getAgeCategory(), tournament)) {
                String msg = String.format("Age category mismatch: Away team '%s' (%s) cannot play in %s tournament '%s'",
                        awayTeam.getCanonicalName(), awayTeam.getAgeCategory(), tournament.getAgeCategory(), tournament.getCanonicalName());
                if (anomalyReportingService != null) {
                    anomalyReportingService.reportLeagueMismatch(createdBy, "AGE_CATEGORY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                }
                throw new IllegalArgumentException(msg);
            }
        }

        // 3. Country / Category check (Domestic leagues only)
        MdmSport sport = tournament.getSport();
        Sport sportObj = sport != null ? sport.asDomain() : null;
        boolean isIndividualSport = sportObj != null ? !sportObj.isTeamSport() : Boolean.FALSE.equals(sport != null ? sport.getIsTeamSport() : true);
        boolean isGenericTourn = isGenericTournamentName(tournament.getCanonicalName());

        if (!isIndividualSport && !isGenericTourn && !Boolean.TRUE.equals(tournament.getIsCup()) && !isInternationalTournament(tournament)) {
            if (isValidDomesticCountry(tournament.getCountry())) {
                if (isValidDomesticCountry(homeTeam.getCountry()) && !isCountryCompatible(tournament, homeTeam)) {
                    String msg = String.format("Country mismatch: Home team '%s' (country=%s #%d) cannot play in domestic tournament '%s' (country=%s #%d)",
                            homeTeam.getCanonicalName(), homeTeam.getCountry().getCanonicalName(), homeTeam.getCountry().getId(),
                            tournament.getCanonicalName(), tournament.getCountry().getCanonicalName(), tournament.getCountry().getId());
                    if (anomalyReportingService != null) {
                        anomalyReportingService.reportLeagueMismatch(createdBy, "COUNTRY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                    }
                    throw new IllegalArgumentException(msg);
                }
                if (isValidDomesticCountry(awayTeam.getCountry()) && !isCountryCompatible(tournament, awayTeam)) {
                    String msg = String.format("Country mismatch: Away team '%s' (country=%s #%d) cannot play in domestic tournament '%s' (country=%s #%d)",
                            awayTeam.getCanonicalName(), awayTeam.getCountry().getCanonicalName(), awayTeam.getCountry().getId(),
                            tournament.getCanonicalName(), tournament.getCountry().getCanonicalName(), tournament.getCountry().getId());
                    if (anomalyReportingService != null) {
                        anomalyReportingService.reportLeagueMismatch(createdBy, "COUNTRY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                    }
                    throw new IllegalArgumentException(msg);
                }
            }
            if (tournament.getCategory() != null) {
                Integer tournCatId = tournament.getCategory().getId();
                boolean tournCatGeneric = isGenericCategory(tournament.getCategory());
                if (homeTeam.getCategory() != null && !tournCatGeneric && !isGenericCategory(homeTeam.getCategory())
                        && !Objects.equals(homeTeam.getCategory().getId(), tournCatId)
                        && !isCountryCompatible(tournament, homeTeam)) {
                    String msg = String.format("Country/Category mismatch: Home team '%s' (category=%d) does not belong to domestic league '%s' (category=%d)",
                            homeTeam.getCanonicalName(), homeTeam.getCategory().getId(), tournament.getCanonicalName(), tournCatId);
                    if (anomalyReportingService != null) {
                        anomalyReportingService.reportLeagueMismatch(createdBy, "COUNTRY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                    }
                    throw new IllegalArgumentException(msg);
                }
                if (awayTeam.getCategory() != null && !tournCatGeneric && !isGenericCategory(awayTeam.getCategory())
                        && !Objects.equals(awayTeam.getCategory().getId(), tournCatId)
                        && !isCountryCompatible(tournament, awayTeam)) {
                    String msg = String.format("Country/Category mismatch: Away team '%s' (category=%d) does not belong to domestic league '%s' (category=%d)",
                            awayTeam.getCanonicalName(), awayTeam.getCategory().getId(), tournament.getCanonicalName(), tournCatId);
                    if (anomalyReportingService != null) {
                        anomalyReportingService.reportLeagueMismatch(createdBy, "COUNTRY_MISMATCH", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                    }
                    throw new IllegalArgumentException(msg);
                }
            }
        }

        // 4. Tournament Roster check (for non-cup leagues with established participants from official catalog)
        if (!Boolean.TRUE.equals(tournament.getIsCup()) && !"SOFASCORE".equalsIgnoreCase(sourceOrigin) && tournament.getSofascoreId() != null) {
            if (tournamentTeamRepository != null) {
                long rosterCount = tournamentTeamRepository.countByTournamentIdAndIsActiveTrue(tournament.getId());
                if (rosterCount >= 16) {
                    if (!tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), homeTeam.getId())) {
                        String msg = String.format("League roster violation: Home team '%s' (#%d) is not a registered participant of league '%s' (#%d)",
                                homeTeam.getCanonicalName(), homeTeam.getId(), tournament.getCanonicalName(), tournament.getId());
                        if (anomalyReportingService != null) {
                            anomalyReportingService.reportLeagueMismatch(createdBy, "ROSTER_VIOLATION", tournament.getId(), tournament.getCanonicalName(), homeTeam.getId(), homeTeam.getCanonicalName(), msg);
                        }
                        throw new IllegalArgumentException(msg);
                    }
                    if (!tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), awayTeam.getId())) {
                        String msg = String.format("League roster violation: Away team '%s' (#%d) is not a registered participant of league '%s' (#%d)",
                                awayTeam.getCanonicalName(), awayTeam.getId(), tournament.getCanonicalName(), tournament.getId());
                        if (anomalyReportingService != null) {
                            anomalyReportingService.reportLeagueMismatch(createdBy, "ROSTER_VIOLATION", tournament.getId(), tournament.getCanonicalName(), awayTeam.getId(), awayTeam.getCanonicalName(), msg);
                        }
                        throw new IllegalArgumentException(msg);
                    }
                }
            }
        }

        // 5. Cyber / Esports Simulation Isolation
        boolean homeIsCyber = CyberMatchDetector.isCyberParticipant(homeTeam.getCanonicalName());
        boolean awayIsCyber = CyberMatchDetector.isCyberParticipant(awayTeam.getCanonicalName());
        Integer tournSportId = tournament.getSport() != null ? tournament.getSport().getId() : null;
        boolean tournIsCyber = (tournSportId != null && (tournSportId == 37 || tournSportId == 41 || tournSportId == 42 || isEsportsSport(tournSportId)))
                || CyberMatchDetector.isCyberLeague(tournament.getCanonicalName());

        if ((homeIsCyber || awayIsCyber) && !tournIsCyber) {
            String msg = String.format("Cyber simulation participant ('%s' vs '%s') cannot play in official non-cyber tournament '%s'",
                    homeTeam.getCanonicalName(), awayTeam.getCanonicalName(), tournament.getCanonicalName());
            if (anomalyReportingService != null) {
                anomalyReportingService.reportLeagueMismatch(createdBy, "CYBER_MISMATCH", tournament.getId(), tournament.getCanonicalName(),
                        homeIsCyber ? homeTeam.getId() : awayTeam.getId(), homeIsCyber ? homeTeam.getCanonicalName() : awayTeam.getCanonicalName(), msg);
            }
            throw new IllegalArgumentException(msg);
        }
    }

    public void validateMatchGenderAndAge(MdmTournament tournament, MdmTeam homeTeam, MdmTeam awayTeam, String createdBy) {
        if (homeTeam == null || awayTeam == null) {
            return;
        }

        String homeGender = normalizeGender(homeTeam.getGender());
        String awayGender = normalizeGender(awayTeam.getGender());
        String homeAge = normalizeAge(homeTeam.getAgeCategory());
        String awayAge = normalizeAge(awayTeam.getAgeCategory());

        Integer sId = tournament != null && tournament.getSport() != null ? tournament.getSport().getId()
                : (homeTeam.getSport() != null ? homeTeam.getSport().getId() : null);
        boolean isCyber = sId != null && (sId == 37 || sId == 6 || sId == 33 || sId == 34 || sId == 35)
                || (tournament != null && CyberMatchDetector.isCyberLeague(tournament.getCanonicalName()));

        boolean isGrassroots = (extractAgeNumber(homeAge) > 0 && extractAgeNumber(homeAge) <= 12)
                || (extractAgeNumber(awayAge) > 0 && extractAgeNumber(awayAge) <= 12)
                || (tournament != null && extractAgeNumber(tournament.getAgeCategory()) > 0 && extractAgeNumber(tournament.getAgeCategory()) <= 12)
                || (tournament != null && tournament.getCanonicalName() != null && (tournament.getCanonicalName().toLowerCase().contains("početnici") || tournament.getCanonicalName().toLowerCase().contains("pocetnici") || tournament.getCanonicalName().toLowerCase().contains("grassroots") || tournament.getCanonicalName().toLowerCase().contains("микст") || tournament.getCanonicalName().toLowerCase().contains("mixed")));

        boolean genderMatch = isGrassroots || isGenderCompatible(homeGender, awayGender);
        boolean ageMatch = isCyber || isAgeCompatible(homeAge, awayAge, tournament);

        boolean isRacketSport = sId == null || sId == 4 || sId == 5 || sId == 13 || sId == 15 || sId == 45 || sId == 59 || sId == 63 || sId == 65;

        boolean homeDoubles = (homeTeam.getSport() != null && (homeTeam.getSport().getId() == 45 || homeTeam.getSport().getId() == 65))
                || (homeTeam.getCanonicalName() != null && homeTeam.getCanonicalName().contains("/"));
        boolean awayDoubles = (awayTeam.getSport() != null && (awayTeam.getSport().getId() == 45 || awayTeam.getSport().getId() == 65))
                || (awayTeam.getCanonicalName() != null && awayTeam.getCanonicalName().contains("/"));
        boolean doublesMatch = !isRacketSport || (homeDoubles == awayDoubles);

        if (!genderMatch || !ageMatch || !doublesMatch) {
            String mismatchType;
            if (!doublesMatch) {
                mismatchType = "DOUBLES_FORMAT_MISMATCH";
            } else if (!genderMatch && !ageMatch) {
                mismatchType = "GENDER_AND_AGE_MISMATCH";
            } else if (!genderMatch) {
                mismatchType = "GENDER_MISMATCH";
            } else {
                mismatchType = "AGE_CATEGORY_MISMATCH";
            }

            String reason = !doublesMatch
                    ? String.format("Doubles format mismatch: Mutant match blocked: Home team '%s' (isDoubles=%s) vs Away team '%s' (isDoubles=%s) in tournament '%s'",
                        homeTeam.getCanonicalName(), homeDoubles, awayTeam.getCanonicalName(), awayDoubles,
                        tournament != null ? tournament.getCanonicalName() : "N/A")
                    : String.format("%s: Mutant match blocked: Home team '%s' (gender=%s, age=%s) vs Away team '%s' (gender=%s, age=%s) in tournament '%s'",
                        !genderMatch ? "Gender mismatch" : "Age category mismatch",
                        homeTeam.getCanonicalName(), homeGender, homeAge,
                        awayTeam.getCanonicalName(), awayGender, awayAge,
                        tournament != null ? tournament.getCanonicalName() : "N/A");

            log.error("🚨 [MDM-MUTANT-BLOCKED] {}", reason);

            if (anomalyReportingService != null) {
                anomalyReportingService.reportMutantMatch(
                        createdBy,
                        tournament,
                        homeTeam,
                        awayTeam,
                        mismatchType,
                        reason
                );
                if (tournament != null) {
                    anomalyReportingService.reportLeagueMismatch(
                            createdBy,
                            mismatchType,
                            tournament.getId(),
                            tournament.getCanonicalName(),
                            !genderMatch ? homeTeam.getId() : awayTeam.getId(),
                            !genderMatch ? homeTeam.getCanonicalName() : awayTeam.getCanonicalName(),
                            reason
                    );
                }
            }

            throw new IllegalArgumentException("🚨 [MDM-MUTANT-BLOCKED] " + reason);
        }
    }

    public static String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return "MALE";
        }
        String g = gender.trim().toUpperCase();
        if (g.contains("FEMALE") || g.contains("WOMEN") || g.contains("WOMAN") || g.contains("LADIES") || g.contains("GIRL")) {
            return "FEMALE";
        }
        if (g.contains("MIX")) {
            return "MIXED";
        }
        return "MALE";
    }

    public static String normalizeAge(String ageCategory) {
        if (ageCategory == null || ageCategory.isBlank()) {
            return "SENIOR";
        }
        return ageCategory.trim().toUpperCase();
    }

    public static boolean isGenderCompatible(String g1, String g2) {
        return Objects.equals(normalizeGender(g1), normalizeGender(g2));
    }

    public static boolean isAcademyAge(String age) {
        if (age == null) return false;
        String a = age.trim().toUpperCase();
        return "YOUTH".equals(a) || "RESERVE".equals(a) || "ACADEMY".equals(a) || extractAgeNumber(a) > 0;
    }

    public static int extractAgeNumber(String age) {
        if (age == null) return 0;
        String a = age.trim().toUpperCase();
        if (a.startsWith("U") && a.length() >= 2) {
            try {
                return Integer.parseInt(a.substring(1));
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    public static boolean isCupTournament(MdmTournament tournament) {
        if (tournament == null) return false;
        if (Boolean.TRUE.equals(tournament.getIsCup())) return true;
        String name = tournament.getCanonicalName();
        if (name != null) {
            String n = name.toLowerCase();
            return n.contains("cup") || n.contains("кубок") || n.contains("trophy")
                    || n.contains("pokal") || n.contains("coppa") || n.contains("copa") || n.contains("coupe");
        }
        return false;
    }

    public static boolean isAgeCompatible(String homeAge, String awayAge, MdmTournament tournament) {
        String a1 = normalizeAge(homeAge);
        String a2 = normalizeAge(awayAge);
        if (Objects.equals(a1, a2)) {
            return true;
        }
        boolean tournAllowsAcademy = isCupTournament(tournament);
        if (tournAllowsAcademy) {
            boolean a1Senior = "SENIOR".equals(a1);
            boolean a2Senior = "SENIOR".equals(a2);
            boolean a1Academy = isAcademyAge(a1);
            boolean a2Academy = isAcademyAge(a2);
            if ((a1Senior && a2Academy) || (a2Senior && a1Academy)) {
                return true;
            }
        }
        // Reserve, B-teams and U20/U21/U23 development squads legitimately compete in SENIOR domestic leagues
        boolean a1ResOrU23 = "RESERVE".equals(a1) || "U23".equals(a1) || "U21".equals(a1) || "U20".equals(a1);
        boolean a2ResOrU23 = "RESERVE".equals(a2) || "U23".equals(a2) || "U21".equals(a2) || "U20".equals(a2);
        if (("SENIOR".equals(a1) && a2ResOrU23) || ("SENIOR".equals(a2) && a1ResOrU23) || (a1ResOrU23 && a2ResOrU23)) {
            return true;
        }
        if (isAcademyAge(a1) && isAcademyAge(a2)) {
            if ("YOUTH".equals(a1) || "YOUTH".equals(a2)) {
                return true;
            }
            int n1 = extractAgeNumber(a1);
            int n2 = extractAgeNumber(a2);
            if (n1 > 0 && n2 > 0 && Math.abs(n1 - n2) <= 1) {
                return true;
            }
            if (tournament != null && isAcademyAge(tournament.getAgeCategory())) {
                int tournLimit = extractAgeNumber(tournament.getAgeCategory());
                if (tournLimit > 0 && n1 <= tournLimit && n2 <= tournLimit) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInternationalTournament(MdmTournament tournament) {
        if (tournament == null) return false;
        MdmSport mdmSport = tournament.getSport();
        if (mdmSport != null) {
            if (isEsportsSport(mdmSport.getId())) {
                return true;
            }
            Sport s = mdmSport.asDomain();
            if (s != null && !s.isTeamSport()) {
                return true;
            }
        }
        if (tournament.getSlug() != null) {
            String sl = tournament.getSlug().toLowerCase();
            if (sl.equals("general") || sl.contains("champions-league") || sl.contains("europa-league")
                    || sl.contains("conference-league") || sl.contains("copa-libertadores")
                    || sl.contains("copa-sudamericana") || sl.contains("afc-") || sl.contains("-afc")
                    || sl.contains("caf-") || sl.contains("concacaf") || sl.contains("euroleague")
                    || sl.contains("eurocup") || sl.contains("vtb") || sl.contains("aba-liga")) {
                return true;
            }
        }
        if (tournament.getCanonicalName() != null && tournament.getCanonicalName().equalsIgnoreCase("general")) {
            return true;
        }
        if (tournament.getCountry() != null) {
            String cName = tournament.getCountry().getCanonicalName() != null ? tournament.getCountry().getCanonicalName().toLowerCase().trim() : "";
            if (cName.contains("international") || cName.contains("world") || cName.contains("europe")
                    || cName.contains("south-america") || cName.contains("asia") || cName.contains("africa")
                    || cName.contains("bwf") || cName.contains("global") || cName.contains("olympic")) {
                return true;
            }
        }
        if (tournament.getCategory() != null) {
            String slug = tournament.getCategory().getSlug();
            if (slug != null) {
                String sLower = slug.toLowerCase();
                if (sLower.contains("international") || sLower.contains("world") || sLower.contains("europe")
                        || sLower.contains("south-america") || sLower.contains("asia") || sLower.contains("africa")
                        || sLower.contains("north-central-america") || sLower.contains("oceania")) {
                    return true;
                }
            }
        }
        String name = tournament.getCanonicalName();
        if (name != null) {
            String nLower = name.toLowerCase();
            if (nLower.contains("champions league") || nLower.contains("europa league") || nLower.contains("conference league")
                    || nLower.contains("лига чемпионов") || nLower.contains("лига европы") || nLower.contains("лига конференций")
                    || nLower.contains("copa libertadores") || nLower.contains("copa sudamericana")
                    || nLower.contains("кубок либертадорес") || nLower.contains("кубок судамерикана") || nLower.contains("южноамериканский кубок")
                    || nLower.contains("world cup") || nLower.contains("чемпионат мира") || nLower.contains("чемпионат европы")
                    || nLower.contains("nations league") || nLower.contains("лига наций")
                    || nLower.contains("friendly") || nLower.contains("товарищеск")
                    || nLower.contains("afc") || nLower.contains("афк") || nLower.contains("caf") || nLower.contains("каф")
                    || nLower.contains("concacaf") || nLower.contains("конкакаф")
                    || nLower.contains("atp") || nLower.contains("wta") || nLower.contains("itf") || nLower.contains("challenger")
                    || nLower.contains("liga pro") || nLower.contains("setka cup") || nLower.contains("tt cup") || nLower.contains("win cup")
                    || nLower.contains("cev") || nLower.contains("challenge cup") || nLower.contains("eurocup") || nLower.contains("еврокубок")
                    || nLower.contains("euroleague") || nLower.contains("евролига")
                    || nLower.contains("vtb") || nLower.contains("втб") || nLower.contains("khl") || nLower.contains("кхл")
                    || nLower.contains("baltic") || nLower.contains("балтийск") || nLower.contains("enbl")
                    || nLower.contains("alps hockey league")
                    || nLower.contains("icehl") || nLower.contains("ebel") || nLower.contains("aba liga") || nLower.contains("aba league")
                    || nLower.contains("адриатическ")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGenericTournamentName(String name) {
        if (name == null || name.isBlank()) return true;
        String s = name.trim().toLowerCase();
        return s.equals("league") || s.equals("general") || s.equals("unclassified")
                || s.startsWith("unassigned") || s.startsWith("unclassified")
                || s.equals("unknown") || s.equals("other")
                || s.equals("tournament") || s.equals("championship") || s.equals("cup")
                || s.equals("мужчины") || s.equals("женщины") || s.equals("men") || s.equals("women")
                || s.equals("лига") || s.equals("чемпионат") || s.equals("кубок")
                || s.equals("товарищеские матчи") || s.equals("friendly matches") || s.equals("friendlies")
                || s.equals("regular season") || s.equals("регулярный сезон")
                || s.equals("плей-офф") || s.equals("playoffs") || s.equals("play-offs")
                || s.equals("квалификация") || s.equals("qualification")
                || s.equals("university league") || s.equals("университетская лига")
                || s.equals("студенческая лига") || s.equals("студенты");
    }

    public static boolean isSubSportOf(int subSportId, int parentSportId) {
        if (subSportId == parentSportId) return true;
        return switch (subSportId) {
            case 60 -> parentSportId == 1; // football-women -> football
            case 71 -> parentSportId == 1; // football-throw-ins -> football
            case 58 -> parentSportId == 3; // basketball-women -> basketball
            case 59 -> parentSportId == 4; // tennis-women -> tennis
            case 61 -> parentSportId == 7; // volleyball-women -> volleyball
            case 62 -> parentSportId == 8; // handball-women -> handball
            case 63 -> parentSportId == 5; // table-tennis-women -> table-tennis
            case 64 -> parentSportId == 2; // ice-hockey-women -> ice-hockey
            case 65 -> parentSportId == 4 || parentSportId == 45; // tennis-doubles-women -> tennis / tennis-doubles
            case 66 -> parentSportId == 16; // futsal-women -> futsal
            case 67 -> parentSportId == 17; // cricket-women -> cricket
            case 68 -> parentSportId == 12; // rugby-women -> rugby
            case 69 -> parentSportId == 19; // waterpolo-women -> waterpolo
            case 70 -> parentSportId == 3 || parentSportId == 29; // basketball-3x3-women -> basketball / basketball-3x3
            case 45 -> parentSportId == 4; // tennis-doubles -> tennis
            case 28 -> parentSportId == 1; // short-football -> football
            case 29 -> parentSportId == 3; // basketball-3x3 -> basketball
            case 30 -> parentSportId == 2; // short-hockey -> ice-hockey
            case 57 -> parentSportId == 3; // basketball-48m -> basketball
            default -> false;
        };
    }

    public static boolean isSportCompatible(int sportA, int sportB) {
        if (sportA == sportB || isSubSportOf(sportA, sportB) || isSubSportOf(sportB, sportA)) return true;
        // Football stats satellites: 43 (corners), 46 (yellow cards), 47 (fouls), 48 (offsides), 49 (goal kicks), 71 (throw-ins) are compatible with 1 (football)
        if (sportA == 1 && (sportB == 43 || sportB == 46 || sportB == 47 || sportB == 48 || sportB == 49 || sportB == 71)) return true;
        if (sportB == 1 && (sportA == 43 || sportA == 46 || sportA == 47 || sportA == 48 || sportA == 49 || sportA == 71)) return true;
        return false;
    }

    public static boolean isValidDomesticCountry(MdmCountry country) {
        if (country == null) return false;
        String iso = country.getIsoCode();
        if (iso == null || iso.isBlank()) return false;
        return !isUniversalPseudoCountry(country.getCanonicalName(), iso);
    }

    public static boolean isUniversalPseudoCountry(String name, String iso) {
        if (name == null) return false;
        return name.contains("international") || name.contains("world") || name.contains("bwf")
                || name.contains("europe") || name.contains("fifa") || name.contains("uefa")
                || name.contains("olympic") || name.contains("global")
                || "INT".equals(iso) || "WRL".equals(iso) || "GLO".equals(iso);
    }

    public static boolean isGenericCategory(MdmCategory cat) {
        if (cat == null) return true;
        String slug = cat.getSlug();
        if (slug == null) return true;
        String s = slug.toLowerCase().trim();
        return s.equals("general") || s.equals("international") || s.equals("world") || s.equals("global");
    }

    public boolean isCountryCompatible(MdmTournament tournament, MdmTeam team) {
        if (tournament == null || team == null) return false;
        if (tournament.getCountry() != null && team.getCountry() != null) {
            return isCountryCompatible(tournament.getCountry(), team.getCountry());
        }
        return false;
    }

    public boolean isCountryCompatible(MdmCountry tc, MdmCountry tmc) {
        if (tc == null || tmc == null) return true;
        if (Objects.equals(tc.getId(), tmc.getId())) return true;

        String tName = tc.getCanonicalName() != null ? tc.getCanonicalName().toLowerCase().trim() : "";
        String tmName = tmc.getCanonicalName() != null ? tmc.getCanonicalName().toLowerCase().trim() : "";
        String tIso = tc.getIsoCode() != null ? tc.getIsoCode().toUpperCase().trim() : "";
        String tmIso = tmc.getIsoCode() != null ? tmc.getIsoCode().toUpperCase().trim() : "";

        // Universal pseudo-countries/federations are compatible with all domestic teams
        if (isUniversalPseudoCountry(tName, tIso) || isUniversalPseudoCountry(tmName, tmIso)) {
            return true;
        }

        // Same country name alias (e.g. "USA" vs "United States")
        if (tName.equals(tmName)) return true;
        if ((tName.equals("usa") || tName.contains("united states") || "US".equals(tIso) || "USA".equals(tIso))
                && (tmName.equals("usa") || tmName.contains("united states") || "US".equals(tmIso) || "USA".equals(tmIso))) {
            return true;
        }

        // NCAA college sports (USA / Canada)
        if (tName.equals("ncaa") || tmName.equals("ncaa")) {
            boolean otherIsUs = tName.equals("usa") || tName.contains("united states") || "US".equals(tIso) || "USA".equals(tIso)
                    || tmName.equals("usa") || tmName.contains("united states") || "US".equals(tmIso) || "USA".equals(tmIso);
            boolean otherIsCa = tName.equals("canada") || "CA".equals(tIso) || "CAN".equals(tIso)
                    || tmName.equals("canada") || "CA".equals(tmIso) || "CAN".equals(tmIso);
            if (otherIsUs || otherIsCa || (tName.equals("ncaa") && tmName.equals("ncaa"))) {
                return true;
            }
        }

        // Allowed cross-border domestic league participants:
        // 1. USA leagues (MLS, USL) allow Canada (Toronto FC, Vancouver Whitecaps, CF Montreal)
        if ((tName.equals("usa") || tName.contains("united states") || "US".equals(tIso) || "USA".equals(tIso))
                && (tmName.equals("canada") || "CA".equals(tmIso) || "CAN".equals(tmIso))) {
            return true;
        }
        // 2. England leagues (Premier League, Championship, etc.) allow Wales (Swansea, Cardiff, Wrexham, Newport)
        if ((tName.equals("england") || "GB".equals(tIso) || "ENG".equals(tIso))
                && (tmName.equals("wales") || "WAL".equals(tmIso))) {
            return true;
        }
        // 3. France leagues allow Monaco
        if ((tName.equals("france") || "FR".equals(tIso) || "FRA".equals(tIso))
                && (tmName.equals("monaco") || "MC".equals(tmIso) || "MCO".equals(tmIso))) {
            return true;
        }
        // 4. Switzerland leagues allow Liechtenstein (Vaduz)
        if ((tName.equals("switzerland") || "CH".equals(tIso) || "CHE".equals(tIso))
                && (tmName.equals("liechtenstein") || "LI".equals(tmIso) || "LIE".equals(tmIso))) {
            return true;
        }
        // 5. Australia leagues allow New Zealand (Wellington Phoenix, Auckland)
        if ((tName.equals("australia") || "AU".equals(tIso) || "AUS".equals(tIso))
                && (tmName.equals("new zealand") || "NZ".equals(tmIso) || "NZL".equals(tmIso))) {
            return true;
        }

        return false;
    }

    public boolean isSyntheticLeague(MatchResolutionRequest request) {
        if (request == null || request.getTournamentName() == null) return false;
        Integer sportId = request.getSportId();
        if (sportId != null && isEsportsSport(sportId)) {
            return false;
        }
        String t = request.getTournamentName().trim();
        return SYNTHETIC_LEAGUE_PATTERN.matcher(t).matches();
    }

    public boolean isEsportsSport(int sportId) {
        return sportId == 6 || (sportId >= 33 && sportId <= 42);
    }

    public boolean isMalformedDoubles(MatchResolutionRequest request) {
        if (request == null || request.getSportId() == null) return false;
        int sportId = request.getSportId();
        boolean isDoublesSport = (sportId == 45 || sportId == 65);
        boolean isRacketSinglesSport = (sportId == 4 || sportId == 5 || sportId == 13 || sportId == 59 || sportId == 63);
        if (!isDoublesSport && !isRacketSinglesSport) return false;

        String h = request.getHomeTeamName();
        String a = request.getAwayTeamName();
        boolean hDoubles = h != null && h.contains("/");
        boolean aDoubles = a != null && a.contains("/");

        if (h != null && a != null && (hDoubles != aDoubles)) {
            return true;
        }

        // Dedicated doubles sport requires both participants to be doubles pairs
        if (isDoublesSport && ((h != null && !hDoubles) || (a != null && !aDoubles))) {
            return true;
        }

        if (request.getHomeTeamId() != null && request.getAwayTeamId() != null && teamRepository != null) {
            MdmTeam homeTeam = teamRepository.findById(request.getHomeTeamId()).orElse(null);
            MdmTeam awayTeam = teamRepository.findById(request.getAwayTeamId()).orElse(null);
            if (homeTeam != null && awayTeam != null) {
                boolean homeDoubles = homeTeam.getCanonicalName().contains("/");
                boolean awayDoubles = awayTeam.getCanonicalName().contains("/");
                if (homeDoubles != awayDoubles) {
                    return true;
                }
                if (isDoublesSport && (!homeDoubles || !awayDoubles)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isStatsOrOutrightMatch(MatchResolutionRequest request) {
        if (request == null) return false;
        Integer sportId = request.getSportId();
        // Dedicated statistical sub-sports (43=corners, 44=hockey shots, 45=yellow cards, 46=fouls, 47=offsides, 48=goal kicks)
        // are legitimate first-class sporting disciplines with their own market distributions
        if (sportId != null && (sportId == 43 || sportId == 44 || sportId == 45 || sportId == 46 || sportId == 47 || sportId == 48 || sportId == 49 || sportId == 71)) {
            return false;
        }
        if (matchesStatsOrOutright(request.getHomeTeamName())) return true;
        if (matchesStatsOrOutright(request.getAwayTeamName())) return true;
        if (matchesStatsOrOutright(request.getTournamentName())) return true;
        return false;
    }

    public boolean matchesStatsOrOutright(String name) {
        if (name == null || name.isBlank()) return false;
        String trimmed = name.trim();
        return STATS_OR_OUTRIGHT_PATTERN.matcher(trimmed).matches()
                || STAT_PREFIX_PATTERN.matcher(trimmed).find()
                || STAT_LEAGUE_PATTERN.matcher(trimmed).find();
    }

    public boolean isSyntheticPlaceholder(String name) {
        if (name == null || name.isBlank()) return false;
        String lower = name.trim().toLowerCase();
        return lower.matches("(?ui)^(?:хозяева|гости|хозяева\\s+тура|гости\\s+тура|team\\s*[12]|home|away|competitor|competitors|участник|участники|соперник|соперники|tbd|tba|winner|победитель)(?:\\s*\\d+|\\s*\\(.*\\))?$");
    }
}
