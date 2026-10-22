package pro.datawiki.igaming.source.unibet.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.url:jdbc:h2:mem:unibet;DB_CLOSE_DELAY=-1;MODE=PostgreSQL}")
    private String url;

    @Value("${spring.datasource.username:sa}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:#{null}}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("🔌 Initializing custom HikariDataSource for unibet: {}", url);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        if (driverClassName != null && !driverClassName.isBlank()) {
            config.setDriverClassName(driverClassName);
        } else if (url != null && url.startsWith("jdbc:h2:")) {
            config.setDriverClassName("org.h2.Driver");
        } else {
            config.setDriverClassName("org.h2.Driver");
        }
        config.setInitializationFailTimeout(-1);
        config.setConnectionTimeout(5000);
        config.setValidationTimeout(3000);
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setPoolName("HikariPool-unibet");
        return new HikariDataSource(config);
    }
}
