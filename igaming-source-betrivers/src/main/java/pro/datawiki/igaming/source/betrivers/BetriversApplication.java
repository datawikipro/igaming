package pro.datawiki.igaming.source.betrivers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "pro.datawiki.igaming.source.betrivers",
        "pro.datawiki.igaming.source.core"
})
@EntityScan(basePackages = {
        "pro.datawiki.igaming.source.core.domain"
})
@EnableJpaRepositories(basePackages = {
        "pro.datawiki.igaming.source.core.repository"
})
public class BetriversApplication {

    public static void main(String[] args) {
        SpringApplication.run(BetriversApplication.class, args);
    }
}
