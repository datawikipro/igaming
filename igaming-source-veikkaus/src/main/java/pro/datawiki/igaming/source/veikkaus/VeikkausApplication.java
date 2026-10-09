package pro.datawiki.igaming.source.veikkaus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "pro.datawiki.igaming.source.veikkaus",
    "pro.datawiki.igaming.source.core"
})
@EnableScheduling
public class VeikkausApplication {
    public static void main(String[] args) {
        SpringApplication.run(VeikkausApplication.class, args);
    }
}