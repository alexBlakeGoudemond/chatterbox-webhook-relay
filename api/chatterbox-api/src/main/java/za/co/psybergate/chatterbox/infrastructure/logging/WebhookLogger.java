package za.co.psybergate.chatterbox.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;

@Component
@Slf4j
public class WebhookLogger {

    public void logMissingSignature() {
        log.warn("[Signature] Missing signature");
    }

    public void logInvalidSignature(String expected, String received) {
        log.warn("[Signature] Invalid signature, expected={}, received={}", expected, received);
    }

    public void logValidSignature() {
        log.info("[Signature] Valid signature");
    }

    public void logReceivedWebhookEvent(String event, String delivery) {
        log.info("[Webhook] Received event={} delivery={}", event, delivery);
    }

    public void logCompletion(long ms) {
        log.debug("[Webhook] Completed in {}ms", ms);
    }

    public void logWebhookReceived(GithubEventDto eventDto) {
        log.debug("[Webhook] received by Github API; DTO: {}", eventDto);
    }

    public void logUnknownEventType(String eventType) {
        log.debug("[Validation] No ConfigurationProperties Found for eventType: {}", eventType);
    }

    public void logUnrecognizedRepository(String repositoryName) {
        log.debug("[Validation] Repository '{}' is not whitelisted as an accepted repository", repositoryName);
    }

    public void logSendingDtoToTeams(GithubEventDto eventDto) {
        log.info("[Delivery] Sending '{}' to MS Teams destination '{}'", eventDto.displayName(), eventDto.teamsDestination());
    }

    public void logTeamsResponse(HttpResponseDto httpResponseDto) {
        log.info("[Delivery] MS Teams Response: {}", httpResponseDto);
    }

    public void logExceptionDetails(Exception ex) {
        log.error("[Exception] Exception encountered: {}, {}", ex.getClass().getSimpleName(), ex.getMessage());
        ex.printStackTrace(); // TODO BlakeGoudemond 2025/12/16 | make better
    }

}
