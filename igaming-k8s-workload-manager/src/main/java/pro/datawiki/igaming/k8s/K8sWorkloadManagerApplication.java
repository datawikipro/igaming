package pro.datawiki.igaming.k8s;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class K8sWorkloadManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(K8sWorkloadManagerApplication.class, args);
    }
}
