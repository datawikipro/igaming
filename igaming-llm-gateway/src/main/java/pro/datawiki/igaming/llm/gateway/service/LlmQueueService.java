package pro.datawiki.igaming.llm.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.llm.gateway.config.KafkaConfig;
import pro.datawiki.igaming.llm.gateway.dto.*;
import pro.datawiki.igaming.llm.gateway.dto.config.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmQueueService {

    private final LlmAdminClient adminClient;
    private final ReactiveRedisTemplate<String, LlmTask> reactiveLlmTaskTemplate;
    private final ReactiveRedisTemplate<String, String> reactiveStringTemplate;
    private final ObjectMapper objectMapper;

    // InMemory cache of configuration from Control Plane
    private final List<ProviderConfigDto> cachedProviders = new CopyOnWriteArrayList<>();
    private final List<ModelConfigDto> cachedModels = new CopyOnWriteArrayList<>();
    private final List<RoutingRuleConfigDto> cachedRoutingRules = new CopyOnWriteArrayList<>();

    private final Map<Long, Long> lastUsedKeyIdByProvider = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Gateway queue initialization...");
        refreshConfigurations().subscribe();
    }

    @Scheduled(fixedRate = 60000)
    public Mono<Void> scheduledRefresh() {
        return refreshConfigurations();
    }

    private Mono<Void> refreshConfigurations() {
        log.debug("Refreshing LLM configurations from admin...");
        
        Mono<List<ProviderConfigDto>> providersMono = adminClient.fetchProviders().collectList();
        Mono<List<ModelConfigDto>> modelsMono = adminClient.fetchModels().collectList();
        Mono<List<RoutingRuleConfigDto>> rulesMono = adminClient.fetchRoutingRules().collectList();

        return Mono.zip(providersMono, modelsMono, rulesMono)
                .doOnNext(tuple -> {
                    cachedProviders.clear();
                    cachedProviders.addAll(tuple.getT1());
                    cachedModels.clear();
                    cachedModels.addAll(tuple.getT2());
                    cachedRoutingRules.clear();
                    cachedRoutingRules.addAll(tuple.getT3());
                    log.info("Refreshed configurations. Providers: {}, Models: {}, Rules: {}", 
                            cachedProviders.size(), cachedModels.size(), cachedRoutingRules.size());
                })
                .then();
    }

    public Mono<LlmSubmitResponse> submit(LlmRequest request) {
        return Mono.defer(() -> {
            ModelConfigDto resolvedModel;
            try {
                resolvedModel = resolveTargetModel(request);
            } catch (IllegalArgumentException e) {
                return Mono.error(e);
            }

            String providerName = getProviderName(resolvedModel);
            String modelName = resolvedModel.getModelId();
            String hash = sha256(providerName + "|" + modelName + "|" + request.getPrompt());
            String cacheKey = "llm:cache:" + hash;

            return reactiveStringTemplate.opsForValue().get(cacheKey)
                    .flatMap(cachedResult -> {
                        log.info("✅ Cache hit hash={} result length={}", hash, cachedResult.length());
                        try {
                            LlmResponse cachedResponse = objectMapper.readValue(cachedResult, LlmResponse.class);
                            return Mono.just(LlmSubmitResponse.builder()
                                    .taskId(null)
                                    .status("COMPLETED")
                                    .cached(true)
                                    .text(cachedResponse.getText())
                                    .tokensUsed(cachedResponse.getTokensUsed())
                                    .finishReason(cachedResponse.getFinishReason())
                                    .build());
                        } catch (Exception e) {
                            log.error("Failed to parse cached response: {}", e.getMessage());
                            return Mono.empty(); // fallback to creating new task if cache invalid
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        UUID taskId = UUID.randomUUID();
                        LlmTask task = LlmTask.builder()
                                .id(taskId)
                                .providerType(providerName)
                                .modelName(modelName)
                                .prompt(request.getPrompt())
                                .systemPrompt(request.getSystemPrompt())
                                .temperature(request.getTemperature() != null ? request.getTemperature() : 0.1)
                                .maxTokens(request.getMaxTokens())
                                .logicalType(request.getLogicalType())
                                .urgency(request.getUrgency())
                                .build();

                        String taskKey = "llm:task:" + taskId;
                        String queueKey = "llm:queue:" + providerName.toLowerCase() + ":" + modelName.toLowerCase();

                        // 1. Save task meta state to Redis
                        // 2. Push task to target queue
                        return reactiveStringTemplate.opsForValue().set(taskKey + ":status", "PENDING", Duration.ofHours(24))
                                .then(reactiveStringTemplate.opsForValue().set(taskKey + ":prompt", request.getPrompt() != null ? request.getPrompt() : "", Duration.ofHours(24)))
                                .then(reactiveStringTemplate.opsForValue().set(taskKey + ":modelName", modelName, Duration.ofHours(24)))
                                .then(reactiveStringTemplate.opsForValue().set(taskKey + ":providerType", providerName, Duration.ofHours(24)))
                                .then(reactiveLlmTaskTemplate.opsForList().leftPush(queueKey, task))
                                .doOnSuccess(v -> log.info("📝 Task submitted id={} provider={} model={}", taskId, providerName, modelName))
                                .thenReturn(LlmSubmitResponse.builder()
                                        .taskId(taskId)
                                        .status("PENDING")
                                        .cached(false)
                                        .build());
                    }));
        });
    }

    public Mono<LlmResponse> generate(LlmRequest request) {
        return submit(request)
                .flatMap(submitResponse -> {
                    if (submitResponse.isCached()) {
                        return Mono.just(LlmResponse.builder()
                                .text(submitResponse.getText())
                                .model(request.getModel())
                                .provider(submitResponse.getStatus())
                                .tokensUsed(submitResponse.getTokensUsed())
                                .finishReason(submitResponse.getFinishReason())
                                .metadata(request.getMetadata())
                                .build());
                    }
                    UUID taskId = submitResponse.getTaskId();
                    
                    return Flux.interval(Duration.ofMillis(500))
                            .flatMap(tick -> getTaskStatusAndResult(taskId))
                            .filter(taskMap -> "COMPLETED".equals(taskMap.get("status")) || "FAILED".equals(taskMap.get("status")))
                            .next()
                            .timeout(Duration.ofSeconds(120))
                            .map(taskMap -> {
                                if ("FAILED".equals(taskMap.get("status"))) {
                                    throw new RuntimeException("LLM task execution failed: " + taskMap.get("errorMessage"));
                                }
                                Integer tokensUsed = 0;
                                try {
                                    tokensUsed = Integer.parseInt(taskMap.getOrDefault("tokensUsed", "0"));
                                } catch (Exception ignored) {}
                                return LlmResponse.builder()
                                        .text(taskMap.get("resultText"))
                                        .model(taskMap.get("modelName"))
                                        .provider(taskMap.get("providerType"))
                                        .tokensUsed(tokensUsed)
                                        .finishReason(taskMap.get("finishReason"))
                                        .metadata(request.getMetadata())
                                        .build();
                            });
                });
    }

    public Mono<LlmTask> claimTask(String providerType, String modelName, String workerId) {
        String resolvedProvider = providerType.toLowerCase().startsWith("gemini") ? "gemini" : providerType.toLowerCase();
        String queueKey = "llm:queue:" + resolvedProvider + ":" + modelName.toLowerCase();

        return reactiveLlmTaskTemplate.opsForList().rightPop(queueKey)
                .flatMap(task -> acquireApiKey(resolvedProvider, modelName)
                        .flatMap(apiKey -> {
                            task.setApiKey(apiKey);
                            String taskKey = "llm:task:" + task.getId();
                            return reactiveStringTemplate.opsForValue().set(taskKey + ":status", "PROCESSING", Duration.ofMinutes(10))
                                    .then(reactiveStringTemplate.opsForValue().set(taskKey + ":worker", workerId, Duration.ofMinutes(10)))
                                    .then(reactiveStringTemplate.opsForValue().set(taskKey + ":provider", resolvedProvider, Duration.ofMinutes(10)))
                                    .then(reactiveStringTemplate.opsForValue().set(taskKey + ":model", modelName, Duration.ofMinutes(10)))
                                    .thenReturn(task);
                        })
                );
    }

    public Mono<Void> completeTask(TaskCompleteRequest request) {
        UUID taskId = request.getTaskId();
        String taskKey = "llm:task:" + taskId;

        return reactiveStringTemplate.opsForValue().get(taskKey + ":status")
                .flatMap(status -> {
                    if ("PROCESSING".equals(status)) {
                        if ("COMPLETED".equals(request.getStatus())) {
                            Map<String, String> values = new HashMap<>();
                            values.put("status", "COMPLETED");
                            values.put("resultText", request.getResultText() != null ? request.getResultText() : "");
                            values.put("tokensUsed", request.getTokensUsed() != null ? String.valueOf(request.getTokensUsed()) : "0");
                            values.put("finishReason", request.getFinishReason() != null ? request.getFinishReason() : "");

                            return saveTaskResult(taskId, values)
                                    .then(cacheResult(taskId, request.getResultText(), request.getTokensUsed(), request.getFinishReason()));
                        } else {
                            // FAILED - check closed loop logic
                            return handleFailedTask(request);
                        }
                    }
                    return Mono.empty();
                })
                .then();
    }

    private Mono<Void> handleFailedTask(TaskCompleteRequest request) {
        UUID taskId = request.getTaskId();
        String taskKey = "llm:task:" + taskId;
        String error = request.getErrorMessage() != null ? request.getErrorMessage().toLowerCase() : "";
        boolean isQuotaError = error.contains("quota") || error.contains("exhausted") || error.contains("limit") || error.contains("429")
                || error.contains("ineligible") || error.contains("restricted") || error.contains("auth") || error.contains("credential")
                || error.contains("invalid") || error.contains("denied") || error.contains("401") || error.contains("403");

        if (isQuotaError) {
            return reactiveStringTemplate.opsForValue().get(taskKey + ":provider")
                    .zipWith(reactiveStringTemplate.opsForValue().get(taskKey + ":model"))
                    .flatMap(tuple -> {
                        String provider = tuple.getT1();
                        String model = tuple.getT2();
                        return acquireApiKey(provider, model) // Get the key that failed
                                .flatMap(failedKey -> {
                                    log.warn("🧹 Closed-Loop: Worker '{}' hit API quota error. Suspending key for 15 mins. Returning task {} to queue.", 
                                            request.getWorkerId(), taskId);
                                    
                                    // Suspend key in Redis
                                    String suspendKey = "llm:suspend:key:" + sha256(failedKey);
                                    return reactiveStringTemplate.opsForValue().set(suspendKey, "true", Duration.ofMinutes(15))
                                            .then(getTaskDetails(taskId, provider, model))
                                            .flatMap(task -> {
                                                String queueKey = "llm:queue:" + provider + ":" + model.toLowerCase();
                                                return reactiveLlmTaskTemplate.opsForList().leftPush(queueKey, task)
                                                        .then(reactiveStringTemplate.opsForValue().set(taskKey + ":status", "PENDING", Duration.ofHours(24)));
                                            });
                                });
                    })
                    .then();
        } else {
            Map<String, String> values = new HashMap<>();
            values.put("status", "FAILED");
            values.put("errorMessage", request.getErrorMessage() != null ? request.getErrorMessage() : "Unknown error");
            return saveTaskResult(taskId, values);
        }
    }

    private Mono<LlmTask> getTaskDetails(UUID taskId, String provider, String model) {
        return reactiveLlmTaskTemplate.opsForList().range("llm:queue:" + provider + ":" + model.toLowerCase(), 0, -1)
                .filter(t -> t.getId().equals(taskId))
                .next()
                .switchIfEmpty(Mono.defer(() -> Mono.just(LlmTask.builder()
                        .id(taskId)
                        .providerType(provider)
                        .modelName(model)
                        .status("PENDING")
                        .build())));
    }

    private Mono<Void> saveTaskResult(UUID taskId, Map<String, String> values) {
        String taskKey = "llm:task:" + taskId;
        return Flux.fromIterable(values.entrySet())
                .flatMap(entry -> reactiveStringTemplate.opsForValue().set(taskKey + ":" + entry.getKey(), entry.getValue(), Duration.ofHours(1)))
                .then();
    }

    private Mono<Void> cacheResult(UUID taskId, String text, Integer tokensUsed, String finishReason) {
        String taskKey = "llm:task:" + taskId;
        return reactiveStringTemplate.opsForValue().get(taskKey + ":provider")
                .zipWith(reactiveStringTemplate.opsForValue().get(taskKey + ":model"))
                .flatMap(tuple -> reactiveStringTemplate.opsForValue().get(taskKey + ":prompt")
                        .flatMap(prompt -> {
                            String provider = tuple.getT1();
                            String model = tuple.getT2();
                            String hash = sha256(provider + "|" + model + "|" + prompt);
                            String cacheKey = "llm:cache:" + hash;

                            LlmResponse response = LlmResponse.builder()
                                    .text(text)
                                    .model(model)
                                    .provider(provider)
                                    .tokensUsed(tokensUsed)
                                    .finishReason(finishReason)
                                    .build();
                            try {
                                String json = objectMapper.writeValueAsString(response);
                                return reactiveStringTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(24));
                            } catch (Exception e) {
                                log.error("Failed to serialize cache payload: {}", e.getMessage());
                                return Mono.empty();
                            }
                        })
                )
                .then();
    }

    private Mono<Map<String, String>> getTaskStatusAndResult(UUID taskId) {
        String taskKey = "llm:task:" + taskId;
        List<String> keys = List.of("status", "resultText", "errorMessage", "modelName", "providerType", "tokensUsed", "finishReason");
        
        return Flux.fromIterable(keys)
                .flatMap(key -> reactiveStringTemplate.opsForValue().get(taskKey + ":" + key)
                        .map(val -> Map.entry(key, val))
                        .defaultIfEmpty(Map.entry(key, ""))
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Mono<String> acquireApiKey(String providerType, String modelName) {
        return Mono.defer(() -> {
            String searchType = providerType.toLowerCase();
            ProviderConfigDto provider = cachedProviders.stream()
                    .filter(p -> p.isActive() && p.getName().toLowerCase().contains(searchType))
                    .findFirst()
                    .orElse(null);

            if (provider == null || provider.getKeys() == null || provider.getKeys().isEmpty()) {
                return Mono.error(new IllegalStateException("No active API keys found for provider: " + providerType));
            }

            List<KeyConfigDto> activeKeys = provider.getKeys().stream()
                    .filter(KeyConfigDto::isActive)
                    .collect(Collectors.toList());

            if (activeKeys.isEmpty()) {
                return Mono.error(new IllegalStateException("No active keys for provider " + provider.getName()));
            }

            // Filter out keys suspended in Redis
            return Flux.fromIterable(activeKeys)
                    .filterWhen(key -> {
                        String suspendKey = "llm:suspend:key:" + sha256(key.getApiKey());
                        return reactiveStringTemplate.hasKey(suspendKey).map(has -> !has);
                    })
                    .collectList()
                    .flatMap(nonSuspendedKeys -> {
                        List<KeyConfigDto> keysToUse = nonSuspendedKeys;
                        if (keysToUse.isEmpty()) {
                            log.warn("⚠️ All keys for provider {} are suspended! Falling back to the first key.", provider.getName());
                            keysToUse = activeKeys;
                        }

                        Long providerId = provider.getId();
                        Long lastKeyId = lastUsedKeyIdByProvider.getOrDefault(providerId, 0L);

                        KeyConfigDto nextKey = null;
                        for (KeyConfigDto k : keysToUse) {
                            if (k.getId() > lastKeyId) {
                                nextKey = k;
                                break;
                            }
                        }

                        if (nextKey == null) {
                            nextKey = keysToUse.get(0);
                        }

                        lastUsedKeyIdByProvider.put(providerId, nextKey.getId());
                        return Mono.just(nextKey.getApiKey());
                    });
        });
    }

    private ModelConfigDto resolveTargetModel(LlmRequest request) {
        if (request.getLogicalType() != null && !request.getLogicalType().isBlank()) {
            // Find matched rule
            List<RoutingRuleConfigDto> rules = cachedRoutingRules.stream()
                    .filter(RoutingRuleConfigDto::isActive)
                    .toList();

            Optional<RoutingRuleConfigDto> ruleOpt = rules.stream()
                    .filter(r -> r.getLogicalType().equalsIgnoreCase(request.getLogicalType())
                            && r.getGoogleRequired() != null && r.getGoogleRequired().equals(request.getUseSearch())
                            && r.getUrgency() != null && r.getUrgency().equalsIgnoreCase(request.getUrgency()))
                    .findFirst();

            if (ruleOpt.isEmpty()) {
                ruleOpt = rules.stream()
                        .filter(r -> r.getLogicalType().equalsIgnoreCase(request.getLogicalType())
                                && r.getGoogleRequired() != null && r.getGoogleRequired().equals(request.getUseSearch()))
                        .findFirst();
            }

            if (ruleOpt.isEmpty()) {
                ruleOpt = rules.stream()
                        .filter(r -> r.getLogicalType().equalsIgnoreCase(request.getLogicalType()))
                        .findFirst();
            }

            if (ruleOpt.isPresent()) {
                return ruleOpt.get().getTargetModel();
            }
        }

        if (request.getModel() != null && !request.getModel().isBlank()) {
            return cachedModels.stream()
                    .filter(m -> m.isActive() && m.getModelId().equalsIgnoreCase(request.getModel()))
                    .findFirst()
                    .orElseGet(() -> cachedModels.stream().filter(ModelConfigDto::isActive).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No active LLM models configured in system.")));
        }

        return cachedModels.stream()
                .filter(ModelConfigDto::isActive)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active LLM models configured in system."));
    }

    private String getProviderName(ModelConfigDto model) {
        return cachedProviders.stream()
                .filter(p -> p.getId().equals(model.getProviderId()))
                .map(ProviderConfigDto::getName)
                .findFirst()
                .orElse("gemini");
    }

    public Mono<List<ModelQueueStats>> getQueueStats() {
        return Flux.fromIterable(cachedModels)
                .filter(ModelConfigDto::isActive)
                .flatMap(model -> {
                    String provider = getProviderName(model);
                    String queueKey = "llm:queue:" + provider.toLowerCase() + ":" + model.getModelId().toLowerCase();
                    return reactiveLlmTaskTemplate.opsForList().size(queueKey)
                            .defaultIfEmpty(0L)
                            .map(size -> ModelQueueStats.builder()
                                    .modelName(model.getModelId())
                                    .providerType(provider)
                                    .pendingCount(size)
                                    .processingCount(0)
                                    .completedCount(0)
                                    .failedCount(0)
                                    .processedPerHour(0)
                                    .build());
                })
                .collectList();
    }

    private String sha256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }
}
