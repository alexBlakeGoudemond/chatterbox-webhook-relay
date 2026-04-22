package com.webhook.relay.chatterbox.adapter.in.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.webhook.relay.chatterbox.application.port.in.webhook.orchestration.WebhookOrchestratorPort;

@RestController
@RequestMapping("${chatterbox.api.prefix}/webhook/github")
@RequiredArgsConstructor
@Slf4j
public class GithubWebhookController {

    private final WebhookOrchestratorPort webhookOrchestratorPort;

    @PostMapping
    public ResponseEntity<String> handleGithubWebhook(@RequestHeader("X-GitHub-Event") String eventType,
                                                      @RequestHeader("X-GitHub-Delivery") String deliveryId,
                                                      @RequestBody JsonNode rawBody) {
        webhookOrchestratorPort.process(eventType, deliveryId, rawBody.toString());
        return ResponseEntity.accepted().body("Webhook received; work underway");
    }

}
