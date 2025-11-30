package za.co.psybergate.chatterbox.application.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/webhook/github")
public class GithubWebhookController {

    @PostMapping
    public ResponseEntity<String> handleGithubWebhook(@RequestHeader Map<String, String> headers,
                                                      @RequestBody String rawBody) {
        log.warn("Github Webhook received by Github API");
        return ResponseEntity.accepted().body("Webhook received");
    }

}
