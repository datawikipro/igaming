package pro.datawiki.igaming.llm.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class LlmAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(LlmAdminApplication.class, args);
    }
}
