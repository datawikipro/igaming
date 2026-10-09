package pro.datawiki.igaming.source.swisslos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"pro.datawiki.igaming"})
@EnableScheduling
public class SwisslosApplication {
    public static void main(String[] args) {
        SpringApplication.run(SwisslosApplication.class, args);
    }
}
