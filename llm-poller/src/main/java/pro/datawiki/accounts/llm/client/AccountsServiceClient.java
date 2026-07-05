package pro.datawiki.accounts.llm.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.accounts.domain.entity.LlmCredential;
import pro.datawiki.accounts.domain.entity.LlmQuotaSnapshot;

import java.util.List;

@Service
public class AccountsServiceClient {

    @Value("${accounts.service.url}")
    private String accountsServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<LlmCredential> getActiveLlmCredentials() {
        String url = accountsServiceUrl + "/api/v1/llm-credentials/active";
        ResponseEntity<List<LlmCredential>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<LlmCredential>>() {}
        );
        return response.getBody();
    }

    public void saveLlmSnapshot(LlmQuotaSnapshot snapshot) {
        String url = accountsServiceUrl + "/api/v1/llm-snapshots";
        HttpEntity<LlmQuotaSnapshot> request = new HttpEntity<>(snapshot);
        restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
    }
}
