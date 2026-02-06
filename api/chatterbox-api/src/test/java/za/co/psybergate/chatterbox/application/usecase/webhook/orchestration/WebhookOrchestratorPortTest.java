package za.co.psybergate.chatterbox.application.usecase.webhook.orchestration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import za.co.psybergate.chatterbox.ChatterboxApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

// TODO BlakeGoudemond 2026/02/06 | write a genuine test
@SpringBootTest(classes = ChatterboxApplication.class)
@ActiveProfiles("test")
class WebhookOrchestratorIT {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {});
    }

}
