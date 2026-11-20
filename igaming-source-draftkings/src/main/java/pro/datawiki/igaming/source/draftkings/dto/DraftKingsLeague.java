package pro.datawiki.igaming.source.draftkings.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rich domain enum encapsulating DraftKings event groups, leagues, and sport mapping.
 */
@Getter
@RequiredArgsConstructor
public enum DraftKingsLeague {
    NBA(88670846, "NBA", "Basketball"),
    NFL(88670847, "NFL", "American Football"),
    MLB(88670848, "MLB", "Baseball"),
    NHL(88670849, "NHL", "Ice Hockey"),
    UFC(88671587, "UFC", "MMA"),
    PREMIER_LEAGUE(88671238, "Premier League", "Soccer");

    private final int eventGroupId;
    private final String leagueName;
    private final String sportName;

    private static final Map<Integer, DraftKingsLeague> BY_ID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(DraftKingsLeague::getEventGroupId, Function.identity()));

    private static final Map<String, DraftKingsLeague> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(DraftKingsLeague::getLeagueName, Function.identity()));

    public static Optional<DraftKingsLeague> findById(int eventGroupId) {
        return Optional.ofNullable(BY_ID.get(eventGroupId));
    }

    public static Optional<DraftKingsLeague> findByName(String name) {
        return Optional.ofNullable(BY_NAME.get(name));
    }

    public static int resolveEventGroupId(String leagueName) {
        if (leagueName != null) {
            DraftKingsLeague league = BY_NAME.get(leagueName);
            if (league != null) {
                return league.getEventGroupId();
            }
            try {
                String numeric = leagueName.replaceAll("[^0-9]", "");
                if (!numeric.isEmpty()) {
                    return Integer.parseInt(numeric);
                }
            } catch (Exception ignored) {}
        }
        return NBA.getEventGroupId();
    }
}
