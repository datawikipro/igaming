package pro.datawiki.igaming.source.atg.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.url:jdbc:h2:mem:atg;DB_CLOSE_DELAY=-1;MODE=PostgreSQL}")
    private String url;

    @Value("${spring.datasource.username:sa}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:#{null}}")
    private String driverClassName;

    @Value("${spring.data.redis.host:igaming-redis.igaming-dev.svc.cluster.local}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("🔌 Initializing custom in-memory HikariDataSource for Atg: {}", url);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        if (driverClassName != null && !driverClassName.isBlank()) {
            config.setDriverClassName(driverClassName);
        } else if (url != null && url.startsWith("jdbc:h2:")) {
            config.setDriverClassName("org.h2.Driver");
        } else {
            config.setDriverClassName("org.postgresql.Driver");
        }
        config.setInitializationFailTimeout(-1);
        config.setConnectionTimeout(5000);
        config.setValidationTimeout(3000);
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setPoolName("HikariPool-Atg");
        return new HikariDataSource(config);
    }

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate() {
        log.info("🔌 Initializing fallback StringRedisTemplate connecting to {}:{}", redisHost, redisPort);
        try {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
            factory.afterPropertiesSet();
            StringRedisTemplate template = new StringRedisTemplate();
            template.setConnectionFactory(factory);
            template.afterPropertiesSet();
            return template;
        } catch (Exception e) {
            log.warn("⚠️ Failed to initialize Redis connection factory: {}. Creating dummy template.", e.getMessage());
            return new StringRedisTemplate();
        }
    }
}
