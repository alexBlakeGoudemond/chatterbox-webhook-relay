package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

@Component
public class WebhookReceivedStoreJpaAdapter implements WebhookReceivedStore {

    private final WebhookReceivedJpaRepository repository;

    public WebhookReceivedStoreJpaAdapter(WebhookReceivedJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public WebhookReceived storeWebhook(WebhookReceived webhook) {
        return repository.save(webhook);
    }

    @Override
    public boolean hasAlreadyBeenStored(String webhookId) {
        return repository.existsByWebhookId(webhookId);
    }

    @Override
    public WebhookReceived getLatestWebhook() {
        return repository.findFirstByOrderByIdDesc();
    }

    @Override
    public void storeWebhook(GithubEventDto eventDto, JsonNode rawBody) {
        WebhookReceived webhookReceived = new WebhookReceived();
    }

}
