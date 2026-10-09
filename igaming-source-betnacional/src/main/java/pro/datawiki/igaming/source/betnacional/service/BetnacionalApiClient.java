package pro.datawiki.igaming.source.betnacional.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarApiClient;
import pro.datawiki.igaming.source.betnacional.config.BetnacionalConfig;

@Service
public class BetnacionalApiClient extends AbstractAltenarApiClient {

    private final BetnacionalConfig config;

    public BetnacionalApiClient(RestTemplate betnacionalRestTemplate, ObjectMapper objectMapper, BetnacionalConfig config) {
        super(betnacionalRestTemplate, objectMapper);
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