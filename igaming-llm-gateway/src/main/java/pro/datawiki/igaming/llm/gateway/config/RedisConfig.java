package pro.datawiki.igaming.llm.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import pro.datawiki.igaming.llm.gateway.dto.LlmTask;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, LlmTask> reactiveLlmTaskTemplate(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<LlmTask> serializer = new Jackson2JsonRedisSerializer<>(LlmTask.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, LlmTask> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, LlmTask> context = builder.value(serializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveStringTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
    }
}
