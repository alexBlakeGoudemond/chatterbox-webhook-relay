package za.co.psybergate.chatterbox.application.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
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
                                                      @RequestBody JsonNode rawBody) {
        String eventType = headers.get("X-GitHub-Event");
        String repositoryName = rawBody.get("repository").get("full_name").toString();
        if (!configurationProperties.acceptsRepository(repositoryName)){
            log.debug("Repository '{}' is not whitelisted as an accepted repository", repositoryName);
            String responseContent =
                    String.format("Webhook received; no work done; unrecognized repository %s", repositoryName);
            return ResponseEntity.ok().body(responseContent);
        }
        if (!configurationProperties.containsEvent(eventType)) {
            log.debug("No ConfigurationProperties Found for eventType: {}", eventType);
            String responseContent =
                    String.format("Webhook received; no work done; unrecognized event %s", eventType);
            return ResponseEntity.ok().body(responseContent);
        }
        // TODO BlakeGoudemond 2025/12/04 | use this information to
        //  - Prepare a Payload for MS Teams
        //  - Send the Payload to MS Teams
        log.warn("Github Webhook received by Github API");
        return ResponseEntity.accepted().body("Webhook received; work underway");
    }

}
