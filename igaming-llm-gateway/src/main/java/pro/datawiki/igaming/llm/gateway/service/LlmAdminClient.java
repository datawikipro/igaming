package pro.datawiki.igaming.llm.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pro.datawiki.igaming.llm.gateway.dto.config.ModelConfigDto;
import pro.datawiki.igaming.llm.gateway.dto.config.ProviderConfigDto;
import pro.datawiki.igaming.llm.gateway.dto.config.RoutingRuleConfigDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class LlmAdminClient {

    private final WebClient webClient;

    public LlmAdminClient(@Value("${app.llm-admin.url:http://igaming-llm-admin:80}") String adminUrl,
                          WebClient.Builder webClientBuilder) {
        log.info("Initializing LlmAdminClient with admin URL: {}", adminUrl);
        this.webClient = webClientBuilder.baseUrl(adminUrl).build();
    }

    public Flux<ProviderConfigDto> fetchProviders() {
        return webClient.get()
                .uri("/api/v1/admin/providers")
                .retrieve()
                .bodyToFlux(ProviderConfigDto.class)
                .onErrorResume(e -> {
                    log.error("Failed to fetch providers from admin: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<ModelConfigDto> fetchModels() {
        return webClient.get()
                .uri("/api/v1/admin/models")
                .retrieve()
                .bodyToFlux(ModelConfigDto.class)
                .onErrorResume(e -> {
                    log.error("Failed to fetch models from admin: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<RoutingRuleConfigDto> fetchRoutingRules() {
        return webClient.get()
                .uri("/api/v1/admin/routing-rules")
                .retrieve()
                .bodyToFlux(RoutingRuleConfigDto.class)
                .onErrorResume(e -> {
                    log.error("Failed to fetch routing rules from admin: {}", e.getMessage());
                    return Flux.empty();
                });
    }
}
