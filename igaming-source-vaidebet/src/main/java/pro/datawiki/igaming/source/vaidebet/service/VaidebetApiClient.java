package pro.datawiki.igaming.source.vaidebet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarApiClient;
import pro.datawiki.igaming.source.vaidebet.config.VaidebetConfig;

@Service
public class VaidebetApiClient extends AbstractAltenarApiClient {

    private final VaidebetConfig config;

    public VaidebetApiClient(RestTemplate vaidebetRestTemplate, ObjectMapper objectMapper, VaidebetConfig config) {
        super(vaidebetRestTemplate, objectMapper);
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