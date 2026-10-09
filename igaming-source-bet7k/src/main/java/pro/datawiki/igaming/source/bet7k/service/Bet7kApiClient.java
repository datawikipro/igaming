package pro.datawiki.igaming.source.bet7k.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarApiClient;
import pro.datawiki.igaming.source.bet7k.config.Bet7kConfig;

@Service
public class Bet7kApiClient extends AbstractAltenarApiClient {

    private final Bet7kConfig config;

    public Bet7kApiClient(RestTemplate bet7kRestTemplate, ObjectMapper objectMapper, Bet7kConfig config) {
        super(bet7kRestTemplate, objectMapper);
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