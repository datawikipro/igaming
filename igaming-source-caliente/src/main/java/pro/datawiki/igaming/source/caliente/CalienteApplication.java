package pro.datawiki.igaming.source.caliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "pro.datawiki.igaming.source.caliente",
        "pro.datawiki.igaming.source.core"
})
@EnableJpaRepositories(basePackages = {
        "pro.datawiki.igaming.source.core.repository"
})
@EntityScan(basePackages = {
        "pro.datawiki.igaming.source.core.domain"
})
@EnableScheduling
public class CalienteApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalienteApplication.class, args);
    }
}
