package pro.datawiki.igaming.vipbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * VIP Telegram Community Bot (@SmartBetVipBot).
 * Dedicated microservice for the closed VIP community of SmartBet.guru.
 *
 * Responsibilities:
 *  - Handle incoming Telegram webhook updates for @SmartBetVipBot
 *  - Manage VIP member subscriptions (join / leave the private channel)
 *  - Broadcast exclusive premium surebet signals to VIP members
 *  - Manage VIP invite links via Telegram Bot API
 *  - Persist VIP member state in PostgreSQL
 */
@SpringBootApplication
@EnableScheduling
public class VipBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(VipBotApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
