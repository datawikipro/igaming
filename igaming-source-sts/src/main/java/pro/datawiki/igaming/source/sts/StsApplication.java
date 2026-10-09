package pro.datawiki.igaming.source.sts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "pro.datawiki.igaming.source.sts",
        "pro.datawiki.igaming.source.core"
})
@EntityScan(basePackages = {
        "pro.datawiki.igaming.source.core.domain"
})
@EnableJpaRepositories(basePackages = {
        "pro.datawiki.igaming.source.core.repository"
})
public class StsApplication {
    public static void main(String[] args) {
        SpringApplication.run(StsApplication.class, args);
    }
}
