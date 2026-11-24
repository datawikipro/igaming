package pro.datawiki.igaming.source.pinnacle.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.live.MatchStatus;
import pro.datawiki.igaming.dto.score.MatchFinalResultDto;
import pro.datawiki.igaming.dto.score.MatchFinalResultDto.CompletionReason;
import pro.datawiki.igaming.dto.score.MatchFinalResultDto.Outcome;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;
import pro.datawiki.igaming.source.core.domain.MatchCache;
import pro.datawiki.igaming.source.core.engine.AbstractBookmakerResultsService;
import pro.datawiki.igaming.source.core.repository.MatchCacheRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Results service for Pinnacle.
 * Polls completed matches and settles results via standard platform pipeline.
 */
@Service
@Slf4j
public class PinnacleResultsService extends AbstractBookmakerResultsService {

    private final PinnacleApiClient apiClient;
    private final String bookmakerName;

    public PinnacleResultsService(MatchCacheRepository matchCacheRepository,
                                  AggregatorClient aggregatorClient,
                                  PinnacleApiClient apiClient,
                                  @Value("${app.bookmaker.name:pinnacle}") String bookmakerName) {
        super(matchCacheRepository, aggregatorClient);
        this.apiClient = apiClient;
        this.bookmakerName = bookmakerName;
    }

    @Override
    public String getBookmakerName() {
        return bookmakerName;
    }

    @Override
    protected boolean hasDedicatedResultsApi() {
        return false;
    }

    @Override
    protected List<MatchFinalResultDto> fetchResultsFromDedicatedApi() {
        return Collections.emptyList();
    }

    @Override
    protected Optional<MatchFinalResultDto> fetchSingleMatchResult(MatchCache match) {
        if (match == null) return Optional.empty();

        if (match.getFinalScore1() != null && match.getFinalScore2() != null) {
            try {
                int s1 = Integer.parseInt(match.getFinalScore1());
                int s2 = Integer.parseInt(match.getFinalScore2());
                Outcome outcome = Outcome.fromScores(s1, s2, true);
                return Optional.of(MatchFinalResultDto.builder()
                        .bookmaker(getBookmakerName())
                        .externalEventId(match.getExternalId())
                        .sport(match.getSportName() != null ? match.getSportName().toLowerCase() : "unknown")
                        .status(MatchStatus.FINISHED)
                        .score1(s1)
                        .score2(s2)
                        .outcome(outcome)
                        .completionReason(CompletionReason.REGULAR_TIME)
                        .confirmedBy("PINNACLE_CACHE_CAPTURE")
                        .confirmedAt(System.currentTimeMillis())
                        .build());
            } catch (Exception ex) {
                log.trace("Could not parse score from match {}: {}", match.getExternalId(), ex.getMessage());
            }
        }

        return Optional.empty();
    }
}
