package pro.datawiki.igaming.source.winner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "pro.datawiki.igaming.source.winner",
    "pro.datawiki.igaming.source.core"
})
@EnableScheduling
public class WinnerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WinnerApplication.class, args);
    }
}