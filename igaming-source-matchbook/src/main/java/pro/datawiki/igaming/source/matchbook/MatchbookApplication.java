package pro.datawiki.igaming.source.matchbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "pro.datawiki.igaming.source.matchbook",
        "pro.datawiki.igaming.source.core"
})
@EntityScan(basePackages = {
        "pro.datawiki.igaming.source.core.domain",
        "pro.datawiki.igaming.source.matchbook.model"
})
@EnableJpaRepositories(basePackages = {
        "pro.datawiki.igaming.source.core.repository",
        "pro.datawiki.igaming.source.matchbook.repository"
})
@EnableScheduling
public class MatchbookApplication {
    public static void main(String[] args) {
        SpringApplication.run(MatchbookApplication.class, args);
    }
}
