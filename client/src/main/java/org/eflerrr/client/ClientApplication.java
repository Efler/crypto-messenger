package org.eflerrr.client;

import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationConfig.class)
@Slf4j
public class ClientApplication {

    public static void main(String[] args) {

        SpringApplication.run(ClientApplication.class, args);

        log.info("/-----------------------------------------\\");
        log.info("|------ CLIENT STARTED SUCCESSFULLY ------|");
        log.info("\\-----------------------------------------/");

    }

}
