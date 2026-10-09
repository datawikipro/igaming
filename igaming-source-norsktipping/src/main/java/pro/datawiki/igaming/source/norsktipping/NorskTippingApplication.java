package pro.datawiki.igaming.source.norsktipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"pro.datawiki.igaming"})
@EnableScheduling
public class NorskTippingApplication {
    public static void main(String[] args) {
        SpringApplication.run(NorskTippingApplication.class, args);
    }
}
