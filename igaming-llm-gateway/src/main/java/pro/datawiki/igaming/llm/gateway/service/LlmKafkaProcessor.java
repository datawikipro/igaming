package pro.datawiki.igaming.llm.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.llm.gateway.config.KafkaConfig;
import pro.datawiki.igaming.llm.gateway.dto.LlmRequest;
import pro.datawiki.igaming.llm.gateway.dto.LlmResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmKafkaProcessor {

    private final LlmQueueService llmQueueService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.LLM_TASKS_TOPIC, groupId = "llm-gateway-group")
    public void consumeTask(String message) {
        log.info("Received task from Kafka: {}", message);
        try {
            LlmRequest request = objectMapper.readValue(message, LlmRequest.class);
            
            llmQueueService.generate(request)
                    .flatMap(response -> {
                        try {
                            String responseJson = objectMapper.writeValueAsString(response);
                            String key = request.getUserId() != null ? request.getUserId() : UUID.randomUUID().toString();
                            log.info("Sending result back to Kafka: key={}, payload={}", key, responseJson);
                            return Mono.fromFuture(() -> kafkaTemplate.send(KafkaConfig.LLM_RESULTS_TOPIC, key, responseJson));
                        } catch (Exception e) {
                            return Mono.error(e);
                        }
                    })
                    .doOnError(err -> log.error("❌ Failed to process Kafka task: {}", err.getMessage()))
                    .subscribe();
                    
        } catch (Exception e) {
            log.error("❌ Failed to parse incoming Kafka message: {}", e.getMessage());
        }
    }
}
