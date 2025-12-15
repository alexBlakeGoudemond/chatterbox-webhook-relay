package za.co.psybergate.chatterbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TODO BlakeGoudemond 2025/12/11 | consider tests for Metrics to assert prometheus is happy
@SpringBootApplication
public class ChatterboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatterboxApplication.class, args);
    }

}
