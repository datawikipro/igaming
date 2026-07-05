package pro.datawiki.accounts.llm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LlmPollerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LlmPollerApplication.class, args);
    }
}
