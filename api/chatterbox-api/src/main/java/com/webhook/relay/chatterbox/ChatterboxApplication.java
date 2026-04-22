package com.webhook.relay.chatterbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ChatterboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatterboxApplication.class, args);
    }

}

