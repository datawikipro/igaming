package pro.datawiki.igaming.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InfraOperatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfraOperatorApplication.class, args);
    }
}
