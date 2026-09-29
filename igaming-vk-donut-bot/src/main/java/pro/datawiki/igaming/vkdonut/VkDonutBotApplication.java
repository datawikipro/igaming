package pro.datawiki.igaming.vkdonut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * VK Donut Community Bot — изолированный микросервис для VK-сообщества SmartBet.guru.
 *
 * Responsibilities:
 *  - Принимать и верифицировать Callback API события от VK (donut_subscription_*)
 *  - Вести реестр донов VK Donut в PostgreSQL (активные/отменённые подписки)
 *  - Отвечать донам через VK Messages API (личные сообщения сообщества)
 *  - Рассылать эксклюзивные Premium-сигналы вилок всем активным донам
 */
@SpringBootApplication
@EnableScheduling
public class VkDonutBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(VkDonutBotApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
