package pro.datawiki.igaming.capture.sofascore.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.capture.sofascore.util.NationalTeamDetector;
import pro.datawiki.igaming.dto.ReferenceFixtureDto;

import java.util.List;

/**
 * Backfill service that pushes national team metadata to the aggregator DB.
 *
 * <p>For each fixture containing at least one national team, this service calls
 * {@code PATCH /api/matches/{matchId}/national-team} on the aggregator, enriching
 * the match_cache rows with:
 * <ul>
 *   <li>{@code team1_is_national}, {@code team1_country_code}, {@code team1_flag_url}</li>
 *   <li>{@code team2_is_national}, {@code team2_country_code}, {@code team2_flag_url}</li>
 * </ul>
 *
 * <p><b>Aggregator endpoint contract:</b> The aggregator matches fixtures by
 * {@code external_fixture_id} + {@code provider} key to identify the correct
 * match_cache row. No match found is silently skipped (HTTP 404 is INFO-logged, not an error).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NationalTeamBackfillService {

    private final RestTemplate restTemplate;

    @Value("${app.aggregator.url:http://localhost:3034}")
    private String aggregatorUrl;

    @Value("${app.backfill.national-teams.enabled:true}")
    private boolean enabled;

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Processes a list of fixtures and pushes national team metadata to the aggregator
     * for any fixture where at least one participant is a national team.
     *
     * @param fixtures list of fixtures to inspect (provider + externalFixtureId used as key)
     * @return count of successfully backfilled fixtures
     */
    public int backfillNationalTeams(List<ReferenceFixtureDto> fixtures) {
        if (!enabled) {
            log.trace("[NationalTeamBackfill] Backfill disabled, skipping.");
            return 0;
        }
        if (fixtures == null || fixtures.isEmpty()) return 0;

        int updated = 0;
        for (ReferenceFixtureDto fixture : fixtures) {
            boolean anyNational = Boolean.TRUE.equals(fixture.getTeam1IsNational())
                    || Boolean.TRUE.equals(fixture.getTeam2IsNational());
            if (!anyNational) continue;

            try {
                boolean sent = sendNationalTeamPatch(fixture);
                if (sent) updated++;
            } catch (Exception e) {
                log.warn("[NationalTeamBackfill] Failed to backfill fixture {}/{}: {}",
                        fixture.getProvider(), fixture.getExternalFixtureId(), e.getMessage());
            }
        }

        if (updated > 0) {
            log.info("[NationalTeamBackfill] Backfilled {} fixtures with national team metadata.", updated);
        }
        return updated;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private boolean sendNationalTeamPatch(ReferenceFixtureDto fixture) {
        String url = aggregatorUrl + "/api/matches/national-team/backfill";

        NationalTeamBackfillRequest request = new NationalTeamBackfillRequest(
                fixture.getProvider(),
                fixture.getExternalFixtureId(),
                fixture.getSport(),
                fixture.getTeam1ExternalId(),
                Boolean.TRUE.equals(fixture.getTeam1IsNational()),
                fixture.getTeam1CountryCode(),
                fixture.getTeam1FlagUrl(),
                fixture.getTeam2ExternalId(),
                Boolean.TRUE.equals(fixture.getTeam2IsNational()),
                fixture.getTeam2CountryCode(),
                fixture.getTeam2FlagUrl()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NationalTeamBackfillRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("[NationalTeamBackfill] Sent backfill for {}/{}: team1={} ({}), team2={} ({})",
                        fixture.getProvider(), fixture.getExternalFixtureId(),
                        fixture.getTeam1Name(), fixture.getTeam1CountryCode(),
                        fixture.getTeam2Name(), fixture.getTeam2CountryCode());
                return true;
            } else {
                log.warn("[NationalTeamBackfill] Aggregator returned {} for fixture {}/{}",
                        response.getStatusCode(), fixture.getProvider(), fixture.getExternalFixtureId());
                return false;
            }
        } catch (HttpClientErrorException.NotFound e) {
            // Match not in aggregator yet — silently skip, will be retried on next sync
            log.info("[NationalTeamBackfill] Fixture {}/{} not found in aggregator (skipping).",
                    fixture.getProvider(), fixture.getExternalFixtureId());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DTO for aggregator backfill endpoint
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Request payload sent to {@code POST /api/matches/national-team/backfill}.
     *
     * <p>The aggregator uses {@code provider} + {@code externalFixtureId} as the lookup key.
     */
    record NationalTeamBackfillRequest(
            @JsonProperty("provider") String provider,
            @JsonProperty("externalFixtureId") String externalFixtureId,
            @JsonProperty("sport") String sport,
            @JsonProperty("team1ExternalId") String team1ExternalId,
            @JsonProperty("team1IsNational") boolean team1IsNational,
            @JsonProperty("team1CountryCode") String team1CountryCode,
            @JsonProperty("team1FlagUrl") String team1FlagUrl,
            @JsonProperty("team2ExternalId") String team2ExternalId,
            @JsonProperty("team2IsNational") boolean team2IsNational,
            @JsonProperty("team2CountryCode") String team2CountryCode,
            @JsonProperty("team2FlagUrl") String team2FlagUrl
    ) {}
}
