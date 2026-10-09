package pro.datawiki.igaming.source.esportesdasorte.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.igaming.source.core.engine.altenar.service.AbstractAltenarApiClient;
import pro.datawiki.igaming.source.esportesdasorte.config.EsportesDaSorteConfig;

@Service
public class EsportesDaSorteApiClient extends AbstractAltenarApiClient {

    private final EsportesDaSorteConfig config;

    public EsportesDaSorteApiClient(RestTemplate esportesdasorteRestTemplate, ObjectMapper objectMapper, EsportesDaSorteConfig config) {
        super(esportesdasorteRestTemplate, objectMapper);
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