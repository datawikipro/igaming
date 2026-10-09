package pro.datawiki.igaming.source.betman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "pro.datawiki.igaming.source.betman",
    "pro.datawiki.igaming.source.core"
})
@EnableScheduling
public class BetmanApplication {
    public static void main(String[] args) {
        SpringApplication.run(BetmanApplication.class, args);
    }
}