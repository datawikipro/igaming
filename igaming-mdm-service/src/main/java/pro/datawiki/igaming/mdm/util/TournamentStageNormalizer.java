package pro.datawiki.igaming.mdm.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes tournament names by extracting tournament stages and rounds
 * (e.g., 1/2 finals, playoffs, qualifications, groups, rounds) into match-level attributes,
 * collapsing split stage-tournaments into their canonical parent tournament.
 */
public final class TournamentStageNormalizer {

    public record StageResult(String cleanTournamentName, String stage, String round) {}

    private record StageMatcher(String stageCode, Pattern pattern) {}

    // Regex to strip statistics props suffixes created by bookmakers as pseudo-tournaments
    private static final Pattern STATS_SUFFIX_PATTERN = Pattern.compile(
            "[,.]\\s*(?:статистика|statistics|stats|удары(?:\\s+в\\s+створ|\\s+от\\s+ворот)?|фолы|офсайды|ауты|corners|угловые|карточки).*$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final List<StageMatcher> STAGE_MATCHERS = List.of(
            // 1/64 - Round of 128
            new StageMatcher("ROUND_OF_128", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(1/64\\s*(?:-я)?\\s*(?:финала|final|de final)?|round of 128)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )),

            // 1/32 - Round of 64
            new StageMatcher("ROUND_OF_64", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(1/32\\s*(?:-я)?\\s*(?:финала|final|de final)?|round of 64)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )),

            // 1/16 - Round of 32
            new StageMatcher("ROUND_OF_32", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(1/16\\s*(?:-я)?\\s*(?:финала|final|de final)?|round of 32)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )),

            // 1/8 - Round of 16
            new StageMatcher("ROUND_OF_16", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(1/8\\s*(?:-я)?\\s*(?:финала|final|de final)?|round of 16|1/8-я)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )),

            // 1/4 - Quarter-final
            new StageMatcher("QUARTER_FINAL", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(1/4\\s*(?:-я)?\\s*(?:финала|final|de final)?|четвертьфинал(?:ы|[а-я]*)?|quarter-?finals?)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )),

            // 1/2 - Semi-final
            new StageMatcher("SEMI_FINAL", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(1/2\\s*(?:-я)?\\s*(?:финала|final|de final)?|полуфинал(?:ы|[а-я]*)?|semi-?finals?)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )),

            // Playoffs / Relegation / Promotion
            new StageMatcher("PLAYOFFS", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(relegation playoffs?|promotion playoffs?|плей-офф(?:ы)?|playoffs?|play-offs?|playoff maçları)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )),

            // Qualification
            new StageMatcher("QUALIFICATION", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(квалификаци[яи]|qualifying|qualification|отборочный (?:раунд|этап)|квал\\.ификац[а-я]*|квал\\.?)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )),

            // Group Stage (e.g., "Group A", "Группа 1", "Групповой этап")
            new StageMatcher("GROUP_STAGE", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(групповой этап|group stage|группа\\s+[a-zа-я0-9]+|group\\s+[a-z0-9]+)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )),

            // Final
            new StageMatcher("FINAL", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(grand final|main final|финал(?:ы|[а-я]*)?|finals?)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )),

            // Regular Season Rounds (e.g., "Тур 5", "Round 12")
            new StageMatcher("REGULAR_SEASON", Pattern.compile(
                    "(?:^|[.,\\-_/]\\s*|\\s+)(регулярный сезон|регулярный чемпионат|regular season|тур\\s+\\d+|раунд\\s+\\d+|round\\s+\\d+|matchday\\s+\\d+)(?:\\s*[.,\\-_/]|\\s*$)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            ))
    );

    private TournamentStageNormalizer() {}

    /**
     * Extracts stage and round attributes from a raw tournament string,
     * stripping out stage markers and statistical suffixes to yield the clean parent tournament name.
     */
    public static StageResult extract(String rawTournamentName) {
        if (rawTournamentName == null || rawTournamentName.isBlank()) {
            return new StageResult(rawTournamentName, null, null);
        }

        String workingName = rawTournamentName.trim();

        // 1. Strip statistics suffix if present
        Matcher statsMatcher = STATS_SUFFIX_PATTERN.matcher(workingName);
        if (statsMatcher.find()) {
            String stripped = workingName.substring(0, statsMatcher.start()).trim();
            if (stripped.length() >= 3) {
                workingName = stripped;
            }
        }

        String extractedStage = null;
        String extractedRound = null;

        // 2. Test each stage matcher
        for (StageMatcher sm : STAGE_MATCHERS) {
            Matcher matcher = sm.pattern().matcher(workingName);
            if (matcher.find()) {
                String matchedText = matcher.group(1).trim();
                extractedStage = sm.stageCode();
                extractedRound = matchedText;

                // Remove the matched stage from tournament name
                String before = workingName.substring(0, matcher.start()).trim();
                String after = workingName.substring(matcher.end()).trim();

                String combined = (before + " " + after).trim();
                // Clean leading/trailing punctuation and double spaces
                combined = cleanPunctuationAndWhitespace(combined);

                // Safeguard: only apply if the parent tournament name remains meaningful (>= 3 chars)
                if (combined.length() >= 3 && !isStageOnlyWord(combined)) {
                    workingName = combined;
                }
                break;
            }
        }

        // Final cleanup of delimiters
        workingName = cleanPunctuationAndWhitespace(workingName);
        if (workingName.isBlank()) {
            workingName = rawTournamentName.trim();
        }

        return new StageResult(workingName, extractedStage, extractedRound);
    }

    private static String cleanPunctuationAndWhitespace(String input) {
        if (input == null) return "";
        // Strip leading/trailing dots, commas, dashes, slashes, semicolons
        String res = input.replaceAll("^[.,\\-_/\\s;]+", "")
                          .replaceAll("[.,\\-_/\\s;]+$", "")
                          .replaceAll("\\s+", " ")
                          .trim();
        return res;
    }

    private static boolean isStageOnlyWord(String s) {
        String lower = s.toLowerCase();
        return lower.equals("женщины") || lower.equals("мужчины") || lower.equals("men") || lower.equals("women")
                || lower.equals("u19") || lower.equals("u21") || lower.equals("u17");
    }
}
