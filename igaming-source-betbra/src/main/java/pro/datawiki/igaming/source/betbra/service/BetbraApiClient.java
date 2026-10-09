package pro.datawiki.igaming.source.betbra.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarApiClient;
import pro.datawiki.igaming.source.betbra.config.BetbraConfig;

@Service
public class BetbraApiClient extends AbstractAltenarApiClient {

    private final BetbraConfig config;

    public BetbraApiClient(RestTemplate betbraRestTemplate, ObjectMapper objectMapper, BetbraConfig config) {
        super(betbraRestTemplate, objectMapper);
        this.config = config;
    }

    @Override
    public String getIntegrationId() {
        return config.getIntegrationId();
    }

    @Override
    public String getBaseUrl() {
        return config.getBaseUrl();
    }
}