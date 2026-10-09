package pro.datawiki.igaming.source.apuestatotal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarApiClient;
import pro.datawiki.igaming.source.apuestatotal.config.ApuestaTotalConfig;

@Service
public class ApuestaTotalApiClient extends AbstractAltenarApiClient {

    private final ApuestaTotalConfig config;

    public ApuestaTotalApiClient(RestTemplate apuestatotalRestTemplate, ObjectMapper objectMapper, ApuestaTotalConfig config) {
        super(apuestatotalRestTemplate, objectMapper);
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