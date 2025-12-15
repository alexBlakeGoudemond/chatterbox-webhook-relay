package za.co.psybergate.chatterbox.infrastructure.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.psybergate.chatterbox.application.webhook.orchestration.GithubWebhookService;

@RestController
@RequestMapping("${chatterbox.api.details.prefix}/webhook/github")
@RequiredArgsConstructor
@Slf4j
public class GithubWebhookController {

    private final GithubWebhookService githubWebhookService;

    @PostMapping
    public ResponseEntity<String> handleGithubWebhook(@RequestHeader("X-GitHub-Event") String eventType,
                                                      @RequestBody JsonNode rawBody) {
        githubWebhookService.process(eventType, rawBody);
        return ResponseEntity.accepted().body("Webhook received; work underway");
    }

}
