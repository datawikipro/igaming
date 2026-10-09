package pro.datawiki.igaming.source.novibet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarApiClient;
import pro.datawiki.igaming.source.novibet.config.NovibetConfig;

@Service
public class NovibetApiClient extends AbstractAltenarApiClient {

    private final NovibetConfig config;

    public NovibetApiClient(RestTemplate novibetRestTemplate, ObjectMapper objectMapper, NovibetConfig config) {
        super(novibetRestTemplate, objectMapper);
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