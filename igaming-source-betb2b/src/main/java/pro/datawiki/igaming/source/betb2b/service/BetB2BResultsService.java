package pro.datawiki.igaming.source.betb2b.service;

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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BetB2BResultsService extends AbstractBookmakerResultsService {

    private final String bookmakerName;

    public BetB2BResultsService(MatchCacheRepository matchCacheRepository,
                                AggregatorClient aggregatorClient,
                                @Value("${app.bookmaker.name:1xbet}") String bookmakerName) {
        super(matchCacheRepository, aggregatorClient);
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

        if (match.getScore1() != null && match.getScore2() != null) {
            try {
                int s1 = Integer.parseInt(match.getScore1().trim());
                int s2 = Integer.parseInt(match.getScore2().trim());

                Outcome outcome = Outcome.fromScores(s1, s2, true);

                MatchFinalResultDto resultDto = MatchFinalResultDto.builder()
                        .bookmaker(getBookmakerName())
                        .externalEventId(match.getExternalId())
                        .sport(match.getSportName() != null ? match.getSportName().toLowerCase() : "unknown")
                        .timestamp(System.currentTimeMillis())
                        .score1(s1)
                        .score2(s2)
                        .status(MatchStatus.FINISHED)
                        .outcome(outcome)
                        .completionReason(CompletionReason.REGULAR_TIME)
                        .confirmedBy("BETB2B_LINE_CLOSED")
                        .confirmedAt(System.currentTimeMillis())
                        .build();

                return Optional.of(resultDto);
            } catch (NumberFormatException e) {
                log.debug("Non-integer score for BetB2B match {}: {}:{}", match.getExternalId(), match.getScore1(), match.getScore2());
            }
        }
        return Optional.empty();
    }
}
