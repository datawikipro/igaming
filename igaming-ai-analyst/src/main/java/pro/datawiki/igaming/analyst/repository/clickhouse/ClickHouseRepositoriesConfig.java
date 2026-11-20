package pro.datawiki.igaming.analyst.repository.clickhouse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClickHouseRepositoriesConfig {

    @Bean
    public TableTennisAnalyticsRepository tableTennisAnalyticsRepository(ClickHouseRepositoryFactory factory) {
        return factory.createRepository(TableTennisAnalyticsRepository.class);
    }

    @Bean
    public FootballAnalyticsRepository footballAnalyticsRepository(ClickHouseRepositoryFactory factory) {
        return factory.createRepository(FootballAnalyticsRepository.class);
    }
}
