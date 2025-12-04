package za.co.psybergate.chatterbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ChatterboxConfigurationProperties.class)
public class ChatterboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatterboxApplication.class, args);
    }

}
