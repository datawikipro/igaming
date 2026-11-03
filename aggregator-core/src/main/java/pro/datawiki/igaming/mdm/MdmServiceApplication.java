/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.SpringApplication
 *  org.springframework.boot.autoconfigure.SpringBootApplication
 *  org.springframework.cache.annotation.EnableCaching
 *  org.springframework.scheduling.annotation.EnableScheduling
 */
package pro.datawiki.igaming.mdm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class MdmServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MdmServiceApplication.class, (String[])args);
    }
}

