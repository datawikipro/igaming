package pro.datawiki.igaming.source.betesporte.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarApiClient;
import pro.datawiki.igaming.source.betesporte.config.BetesporteConfig;

@Service
public class BetesporteApiClient extends AbstractAltenarApiClient {

    private final BetesporteConfig config;

    public BetesporteApiClient(RestTemplate betesporteRestTemplate, ObjectMapper objectMapper, BetesporteConfig config) {
        super(betesporteRestTemplate, objectMapper);
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