package pro.datawiki.igaming.source.stoiximan.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Proxy;

@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate() {
        log.info("Initializing standalone no-op StringRedisTemplate (zero Redis dependency)");
        RedisConnection mockConnection = (RedisConnection) Proxy.newProxyInstance(
                RedisConnection.class.getClassLoader(),
                new Class<?>[]{RedisConnection.class},
                (proxy, method, args) -> null
        );
        RedisConnectionFactory mockFactory = (RedisConnectionFactory) Proxy.newProxyInstance(
                RedisConnectionFactory.class.getClassLoader(),
                new Class<?>[]{RedisConnectionFactory.class},
                (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName())) {
                        return mockConnection;
                    }
                    return null;
                }
        );
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(mockFactory);
        template.afterPropertiesSet();
        return template;
    }
}
