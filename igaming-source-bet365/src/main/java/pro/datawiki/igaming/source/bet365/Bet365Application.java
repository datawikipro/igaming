package pro.datawiki.igaming.source.bet365;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"pro.datawiki.igaming.source.bet365", "pro.datawiki.igaming.source.core"})
@EntityScan(basePackages = {"pro.datawiki.igaming.source.bet365.domain", "pro.datawiki.igaming.source.core.domain"})
@EnableJpaRepositories(basePackages = {"pro.datawiki.igaming.source.bet365.repository", "pro.datawiki.igaming.source.core.repository"})
@EnableScheduling
public class Bet365Application {

    public static void main(String[] args) {
        SpringApplication.run(Bet365Application.class, args);
    }
}
