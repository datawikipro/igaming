package pro.datawiki.igaming.source.tenbet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "pro.datawiki.igaming.source.tenbet",
        "pro.datawiki.igaming.source.core"
})
@EnableJpaRepositories(basePackages = {
        "pro.datawiki.igaming.source.core.repository"
})
@EntityScan(basePackages = {
        "pro.datawiki.igaming.source.core.domain"
})
@EnableScheduling
public class TenBetApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenBetApplication.class, args);
    }
}
