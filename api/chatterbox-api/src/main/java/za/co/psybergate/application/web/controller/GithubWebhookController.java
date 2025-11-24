package za.co.psybergate.application.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/webhook/github")
public class GithubWebhookController {

    @PostMapping
    public ResponseEntity<String> handleGithubWebhook(@RequestBody String body, @RequestHeader Map<String,String> headers){
        System.out.println("GithubWebhookController.handleGithubWebhook");
        return new ResponseEntity<>("Payload received", HttpStatus.ACCEPTED);
    }
}
