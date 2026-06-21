package pro.datawiki.igaming.llm.gateway.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String LLM_TASKS_TOPIC = "llm-normalization-tasks";
    public static final String LLM_RESULTS_TOPIC = "llm-normalization-results";

    @Bean
    public NewTopic llmTasksTopic() {
        return TopicBuilder.name(LLM_TASKS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic llmResultsTopic() {
        return TopicBuilder.name(LLM_RESULTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
