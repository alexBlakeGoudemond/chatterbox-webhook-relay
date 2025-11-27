package za.co.psybergate.chatterbox.application.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.psybergate.chatterbox.application.core.utility.EncryptionUtilitiesImpl;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/webhook/github")
public class GithubWebhookController {

    private final EncryptionUtilitiesImpl encryptionUtilities;

    @Value("${webhook.github.secret}")
    private String webhookSecret;

    public GithubWebhookController(EncryptionUtilitiesImpl encryptionUtilities) {
        this.encryptionUtilities = encryptionUtilities;
    }

    @PostMapping
    public ResponseEntity<String> handleGithubWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature256,
            @RequestBody String rawBody) {
        if (signature256 == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing signature");
        }

        String expected = encryptionUtilities.encryptUsingSHA256(webhookSecret, rawBody);
        if (!encryptionUtilities.isIdentical(expected, signature256)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        log.warn("Github Webhook received by Github API");
        return ResponseEntity.accepted().body("Webhook received");
    }

}
