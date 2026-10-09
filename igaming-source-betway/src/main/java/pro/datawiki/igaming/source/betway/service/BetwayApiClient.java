package pro.datawiki.igaming.source.betway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.betway.config.BetwayConfig;
import pro.datawiki.igaming.source.core.engine.sbtech.service.AbstractSBTechApiClient;

@Service
public class BetwayApiClient extends AbstractSBTechApiClient {

    private final BetwayConfig config;

    public BetwayApiClient(RestTemplate betwayRestTemplate, ObjectMapper objectMapper, BetwayConfig config) {
        super(betwayRestTemplate, objectMapper);
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
