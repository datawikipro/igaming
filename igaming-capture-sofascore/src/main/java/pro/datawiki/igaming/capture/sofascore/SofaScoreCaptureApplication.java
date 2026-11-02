package pro.datawiki.igaming.capture.sofascore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication(scanBasePackages = {
        "pro.datawiki.igaming.capture.sofascore",
        "pro.datawiki.igaming.mdm.client"
})
@EnableScheduling
public class SofaScoreCaptureApplication {

    public static void main(String[] args) {
        SpringApplication.run(SofaScoreCaptureApplication.class, args);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("sofa-sched-");
        scheduler.initialize();
        return scheduler;
    }

}
