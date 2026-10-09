package pro.datawiki.igaming.source.tenbet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.engine.sbtech.service.AbstractSBTechApiClient;
import pro.datawiki.igaming.source.tenbet.config.TenBetConfig;

@Service
public class TenBetApiClient extends AbstractSBTechApiClient {

    private final TenBetConfig config;

    public TenBetApiClient(RestTemplate tenBetRestTemplate, ObjectMapper objectMapper, TenBetConfig config) {
        super(tenBetRestTemplate, objectMapper);
        this.config = config;
    }

    @Override
    public String getBrand() {
        return config.getBrand();
    }

    @Override
    public String getBaseUrl() {
        return config.getBaseUrl();
    }
}
