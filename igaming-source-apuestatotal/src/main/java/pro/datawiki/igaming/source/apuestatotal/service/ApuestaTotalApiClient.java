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

    @Override
    public String getOrigin() {
        if (config.getOrigin() != null && !config.getOrigin().isBlank()) {
            return config.getOrigin();
        }
        return super.getOrigin();
    }

    @Override
    public String getCulture() {
        return config.getCulture() != null ? config.getCulture() : super.getCulture();
    }

    @Override
    public String getCountryCode() {
        return config.getCountryCode() != null ? config.getCountryCode() : super.getCountryCode();
    }

    @Override
    public int getTimezoneOffset() {
        return config.getTimezoneOffset() != 0 ? config.getTimezoneOffset() : super.getTimezoneOffset();
    }
}
