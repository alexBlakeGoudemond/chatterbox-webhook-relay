package com.webhook.relay.chatterbox;

import com.webhook.relay.chatterbox.test.container.AbstractPostgresTestContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class ChatterboxApplicationTests extends AbstractPostgresTestContainer {

    @Test
    void contextLoads() {
        Assertions.assertTrue(true);
    }

}
