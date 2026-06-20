package pro.datawiki.igaming.source.digitain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "pro.datawiki.igaming.source.digitain",
        "pro.datawiki.igaming.source.core"
})
@EnableScheduling
public class DigitainApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitainApplication.class, args);
    }
}
