package pro.datawiki.igaming.source.tab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"pro.datawiki.igaming"})
@EnableScheduling
public class TabApplication {
    public static void main(String[] args) {
        SpringApplication.run(TabApplication.class, args);
    }
}
