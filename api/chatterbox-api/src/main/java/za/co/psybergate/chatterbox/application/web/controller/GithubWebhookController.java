package za.co.psybergate.chatterbox.application.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/webhook/github")
@RequiredArgsConstructor
public class GithubWebhookController {

    private final ChatterboxConfigurationProperties configurationProperties;

    @PostMapping
    public ResponseEntity<String> handleGithubWebhook(@RequestHeader Map<String, String> headers,
                                                      @RequestBody String rawBody) {
        String eventType = headers.get("X-GitHub-Event");
        if (configurationProperties.containsEvent(eventType)) {
            log.warn("configurationProperties exist for eventType: {}, config: {}", eventType, configurationProperties);
        }
        log.warn("Github Webhook received by Github API");
        return ResponseEntity.accepted().body("Webhook received");
    }

}
