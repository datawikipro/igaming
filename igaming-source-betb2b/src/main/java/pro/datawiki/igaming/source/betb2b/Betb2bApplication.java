package pro.datawiki.igaming.source.betb2b;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"pro.datawiki.igaming.source.betb2b", "pro.datawiki.igaming.source.core"})
@EnableScheduling
@EntityScan(basePackages = {"pro.datawiki.igaming.source.core.domain", "pro.datawiki.igaming.source.core.entity"})
@EnableJpaRepositories(basePackages = "pro.datawiki.igaming.source.core.repository")
public class Betb2bApplication {
    public static void main(String[] args) {
        SpringApplication.run(Betb2bApplication.class, args);
    }
}
