package pro.datawiki.igaming.capture.esports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Esports Match & Statistics Capture Service for SmartBet.guru.
 * Top-Down Ingestion Blueprint integration for CS2 (HLTV), Dota 2 (OpenDota), and Valorant (VLR.gg).
 */
@SpringBootApplication(scanBasePackages = {
        "pro.datawiki.igaming.capture.esports",
        "pro.datawiki.igaming.mdm.client"
})
@EnableScheduling
public class EsportsCaptureApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsportsCaptureApplication.class, args);
    }
}
