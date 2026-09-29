package pro.datawiki.igaming.boosty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Boosty.to Community Bot — dedicated isolated pod for SmartBet.guru Boosty account.
 *
 * <p>Features:
 * <ul>
 *   <li>Polling Boosty API for new subscribers / donors</li>
 *   <li>Collecting and storing comments from donors (paying subscribers)</li>
 *   <li>Isolated PostgreSQL database (igaming_boosty)</li>
 *   <li>Forwarding donor welcome/farewell messages via Telegram</li>
 * </ul>
 *
 * <p>Boosty does not provide native webhooks; polling is the standard approach
 * for community projects (see community libraries: akovardin/boosty, PyBoostyApi).
 */
@SpringBootApplication
@EnableScheduling
public class BoostyBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoostyBotApplication.class, args);
    }
}
