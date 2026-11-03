package pro.datawiki.igaming.capture.fbref;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FbrefCaptureApplication {

    public static void main(String[] args) {
        SpringApplication.run(FbrefCaptureApplication.class, args);
    }
}
