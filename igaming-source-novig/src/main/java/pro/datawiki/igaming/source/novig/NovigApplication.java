package pro.datawiki.igaming.source.novig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "pro.datawiki.igaming.source.novig",
        "pro.datawiki.igaming.source.core"
})
@EntityScan(basePackages = {
        "pro.datawiki.igaming.source.core.model",
        "pro.datawiki.igaming.source.novig.model"
})
@EnableJpaRepositories(basePackages = {
        "pro.datawiki.igaming.source.core.repository",
        "pro.datawiki.igaming.source.novig.repository"
})
@EnableScheduling
public class NovigApplication {
    public static void main(String[] args) {
        SpringApplication.run(NovigApplication.class, args);
    }
}
