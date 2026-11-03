/*
 * Decompiled with CFR 0.152.
 */
package pro.datawiki.igaming.mdm.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TournamentStageNormalizer {
    private static final Pattern STATS_SUFFIX_PATTERN = Pattern.compile("[,.]\\s*(?:\u0441\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430|statistics|stats|\u0443\u0434\u0430\u0440\u044b(?:\\s+\u0432\\s+\u0441\u0442\u0432\u043e\u0440|\\s+\u043e\u0442\\s+\u0432\u043e\u0440\u043e\u0442)?|\u0444\u043e\u043b\u044b|\u043e\u0444\u0441\u0430\u0439\u0434\u044b|\u0430\u0443\u0442\u044b|corners|\u0443\u0433\u043b\u043e\u0432\u044b\u0435|\u043a\u0430\u0440\u0442\u043e\u0447\u043a\u0438).*$", 66);
    private static final List<StageMatcher> STAGE_MATCHERS = List.of(new StageMatcher("ROUND_OF_128", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(1/64\\s*(?:-\u044f)?\\s*(?:\u0444\u0438\u043d\u0430\u043b\u0430|final|de final)?|round of 128)(?:\\s*[.,\\-_/]|\\s*$)", 66)), new StageMatcher("ROUND_OF_64", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(1/32\\s*(?:-\u044f)?\\s*(?:\u0444\u0438\u043d\u0430\u043b\u0430|final|de final)?|round of 64)(?:\\s*[.,\\-_/]|\\s*$)", 66)), new StageMatcher("ROUND_OF_32", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(1/16\\s*(?:-\u044f)?\\s*(?:\u0444\u0438\u043d\u0430\u043b\u0430|final|de final)?|round of 32)(?:\\s*[.,\\-_/]|\\s*$)", 66)), new StageMatcher("ROUND_OF_16", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(1/8\\s*(?:-\u044f)?\\s*(?:\u0444\u0438\u043d\u0430\u043b\u0430|final|de final)?|round of 16|1/8-\u044f)(?:\\s*[.,\\-_/]|\\s*$)", 66)), new StageMatcher("QUARTER_FINAL", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(1/4\\s*(?:-\u044f)?\\s*(?:\u0444\u0438\u043d\u0430\u043b\u0430|final|de final)?|\u0447\u0435\u0442\u0432\u0435\u0440\u0442\u044c\u0444\u0438\u043d\u0430\u043b(?:\u044b|[\u0430-\u044f]*)?|quarter-?finals?)(?:\\s*[.,\\-_/]|\\s*$)", 66)), new StageMatcher("SEMI_FINAL", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(1/2\\s*(?:-\u044f)?\\s*(?:\u0444\u0438\u043d\u0430\u043b\u0430|final|de final)?|\u043f\u043e\u043b\u0443\u0444\u0438\u043d\u0430\u043b(?:\u044b|[\u0430-\u044f]*)?|semi-?finals?)(?:\\s*[.,\\-_/]|\\s*$)", 66)), new StageMatcher("PLAYOFFS", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(relegation playoffs?|promotion playoffs?|\u043f\u043b\u0435\u0439-\u043e\u0444\u0444(?:\u044b)?|playoffs?|play-offs?|playoff ma\u00e7lar\u0131)(?:\\s*[.,\\-_/]|\\s*$)", 66)), new StageMatcher("QUALIFICATION", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(\u043a\u0432\u0430\u043b\u0438\u0444\u0438\u043a\u0430\u0446\u0438[\u044f\u0438]|qualifying|qualification|\u043e\u0442\u0431\u043e\u0440\u043e\u0447\u043d\u044b\u0439 (?:\u0440\u0430\u0443\u043d\u0434|\u044d\u0442\u0430\u043f)|\u043a\u0432\u0430\u043b\\.\u0438\u0444\u0438\u043a\u0430\u0446[\u0430-\u044f]*|\u043a\u0432\u0430\u043b\\.?)(?:\\s*[.,\\-_/]|\\s*$)", 66)), new StageMatcher("GROUP_STAGE", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(\u0433\u0440\u0443\u043f\u043f\u043e\u0432\u043e\u0439 \u044d\u0442\u0430\u043f|group stage|\u0433\u0440\u0443\u043f\u043f\u0430\\s+[a-z\u0430-\u044f0-9]+|group\\s+[a-z0-9]+)(?:\\s*[.,\\-_/]|\\s*$)", 66)), new StageMatcher("FINAL", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(grand final|main final|\u0444\u0438\u043d\u0430\u043b(?:\u044b|[\u0430-\u044f]*)?|finals?)(?:\\s*[.,\\-_/]|\\s*$)", 66)), new StageMatcher("REGULAR_SEASON", Pattern.compile("(?:^|[.,\\-_/]\\s*|\\s+)(\u0440\u0435\u0433\u0443\u043b\u044f\u0440\u043d\u044b\u0439 \u0441\u0435\u0437\u043e\u043d|\u0440\u0435\u0433\u0443\u043b\u044f\u0440\u043d\u044b\u0439 \u0447\u0435\u043c\u043f\u0438\u043e\u043d\u0430\u0442|regular season|\u0442\u0443\u0440\\s+\\d+|\u0440\u0430\u0443\u043d\u0434\\s+\\d+|round\\s+\\d+|matchday\\s+\\d+)(?:\\s*[.,\\-_/]|\\s*$)", 66)));

    private TournamentStageNormalizer() {
    }

    public static StageResult extract(String rawTournamentName) {
        String stripped;
        if (rawTournamentName == null || rawTournamentName.isBlank()) {
            return new StageResult(rawTournamentName, null, null);
        }
        String workingName = rawTournamentName.trim();
        Matcher statsMatcher = STATS_SUFFIX_PATTERN.matcher(workingName);
        if (statsMatcher.find() && (stripped = workingName.substring(0, statsMatcher.start()).trim()).length() >= 3) {
            workingName = stripped;
        }
        String extractedStage = null;
        String extractedRound = null;
        for (StageMatcher sm : STAGE_MATCHERS) {
            Matcher matcher = sm.pattern().matcher(workingName);
            if (!matcher.find()) continue;
            String matchedText = matcher.group(1).trim();
            extractedStage = sm.stageCode();
            extractedRound = matchedText;
            String before = workingName.substring(0, matcher.start()).trim();
            String after = workingName.substring(matcher.end()).trim();
            String combined = (before + " " + after).trim();
            if ((combined = TournamentStageNormalizer.cleanPunctuationAndWhitespace(combined)).length() < 3 || TournamentStageNormalizer.isStageOnlyWord(combined)) break;
            workingName = combined;
            break;
        }
        if ((workingName = TournamentStageNormalizer.cleanPunctuationAndWhitespace(workingName)).isBlank()) {
            workingName = rawTournamentName.trim();
        }
        return new StageResult(workingName, extractedStage, extractedRound);
    }

    private static String cleanPunctuationAndWhitespace(String input) {
        if (input == null) {
            return "";
        }
        String res = input.replaceAll("^[.,\\-_/\\s;]+", "").replaceAll("[.,\\-_/\\s;]+$", "").replaceAll("\\s+", " ").trim();
        return res;
    }

    private static boolean isStageOnlyWord(String s) {
        String lower = s.toLowerCase();
        return lower.equals("\u0436\u0435\u043d\u0449\u0438\u043d\u044b") || lower.equals("\u043c\u0443\u0436\u0447\u0438\u043d\u044b") || lower.equals("men") || lower.equals("women") || lower.equals("u19") || lower.equals("u21") || lower.equals("u17");
    }

    public record StageResult(String cleanTournamentName, String stage, String round) {
    }

    private record StageMatcher(String stageCode, Pattern pattern) {
    }
}

