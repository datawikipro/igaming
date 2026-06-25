package com.antigravity.overlay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AntigravityOverlayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AntigravityOverlayApplication.class, args);
    }
}
