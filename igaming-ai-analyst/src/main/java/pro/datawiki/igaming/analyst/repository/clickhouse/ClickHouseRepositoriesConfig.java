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

    @Bean
    public BasketballAnalyticsRepository basketballAnalyticsRepository(ClickHouseRepositoryFactory factory) {
        return factory.createRepository(BasketballAnalyticsRepository.class);
    }

    @Bean
    public TennisAnalyticsRepository tennisAnalyticsRepository(ClickHouseRepositoryFactory factory) {
        return factory.createRepository(TennisAnalyticsRepository.class);
    }

    @Bean
    public IceHockeyAnalyticsRepository iceHockeyAnalyticsRepository(ClickHouseRepositoryFactory factory) {
        return factory.createRepository(IceHockeyAnalyticsRepository.class);
    }

    @Bean
    public Cs2AnalyticsRepository cs2AnalyticsRepository(ClickHouseRepositoryFactory factory) {
        return factory.createRepository(Cs2AnalyticsRepository.class);
    }
}
