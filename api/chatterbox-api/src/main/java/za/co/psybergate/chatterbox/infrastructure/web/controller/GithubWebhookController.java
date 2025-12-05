package za.co.psybergate.chatterbox.infrastructure.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.psybergate.chatterbox.application.webhook.service.WebhookService;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/webhook/github")
@RequiredArgsConstructor
public class GithubWebhookController {

    private final WebhookService webhookService;

    @PostMapping
    public ResponseEntity<String> handleGithubWebhook(@RequestHeader Map<String, String> headers,
                                                      @RequestBody JsonNode rawBody) {
        String eventType = headers.get("X-GitHub-Event");
        webhookService.process(eventType, rawBody);
        return ResponseEntity.accepted().body("Webhook received; work underway");
    }

}
