package pro.datawiki.accounts.cloud.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pro.datawiki.accounts.domain.entity.CloudCredential;
import pro.datawiki.accounts.domain.entity.CloudSnapshot;
import pro.datawiki.accounts.domain.entity.ManagedServer;

import java.util.List;

@Component
public class AccountsServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AccountsServiceClient(@Value("${accounts.service.url:http://accounts-service:8091}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public List<CloudCredential> getActiveCloudCredentials() {
        String url = baseUrl + "/api/v1/cloud-credentials/active";
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CloudCredential>>() {}
        ).getBody();
    }

    public void saveCloudSnapshot(CloudSnapshot snapshot) {
        String url = baseUrl + "/api/v1/cloud-snapshots";
        restTemplate.postForLocation(url, snapshot);
    }

    public void saveOrUpdateServer(ManagedServer server) {
        String url = baseUrl + "/api/v1/servers";
        restTemplate.postForLocation(url, server);
    }

    public void updateServerStatus(String instanceName, String status) {
        String url = baseUrl + "/api/v1/servers/" + instanceName;
        // In order to perform a PUT with body we can wrap the status in a map/entity,
        // but wait, the prompt says: PUT /api/v1/servers/{instanceName}
        // Assuming it expects a simple string or JSON object depending on how it's implemented.
        // Usually we might just put the string as the body. Let's send the status as raw string body or query param?
        // Let's send it in the body.
        restTemplate.put(url, status);
    }

    public void deleteServer(String instanceName) {
        String url = baseUrl + "/api/v1/servers/" + instanceName;
        restTemplate.delete(url);
    }

    public List<ManagedServer> getAllServers() {
        String url = baseUrl + "/api/v1/servers";
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ManagedServer>>() {}
        ).getBody();
    }
}
