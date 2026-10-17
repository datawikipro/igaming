package pro.datawiki.igaming.analyst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiAnalystApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAnalystApplication.class, args);
    }
}
