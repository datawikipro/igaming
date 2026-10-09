package pro.datawiki.igaming.source.crickex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"pro.datawiki.igaming"})
@EntityScan(basePackages = {
        "pro.datawiki.igaming.source.core.domain"
})
@EnableJpaRepositories(basePackages = {
        "pro.datawiki.igaming.source.core.repository"
})
@EnableScheduling
public class CrickexApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrickexApplication.class, args);
    }
}
