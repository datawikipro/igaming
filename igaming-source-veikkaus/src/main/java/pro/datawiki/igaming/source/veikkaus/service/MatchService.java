package pro.datawiki.igaming.source.veikkaus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.dto.OddsUpdateRequest;
import pro.datawiki.igaming.source.veikkaus.dto.VeikkausEventDto;
import pro.datawiki.igaming.source.core.aggregator.AggregatorClient;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchService {

    private final VeikkausOddsMapper oddsMapper;
    private final AggregatorClient aggregatorClient;

    public void processEvents(List<VeikkausEventDto> events) {
        if (events == null || events.isEmpty()) return;

        for (VeikkausEventDto event : events) {
            try {
                OddsUpdateRequest request = oddsMapper.mapToOddsUpdate(event);
                if (request != null) {
                    aggregatorClient.pushOddsUpdate(request);
                }
            } catch (Exception e) {
                log.warn("Failed to process event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}