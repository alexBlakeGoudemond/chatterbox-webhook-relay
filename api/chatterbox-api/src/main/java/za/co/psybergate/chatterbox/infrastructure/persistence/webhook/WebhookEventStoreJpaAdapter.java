package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

@Component
public class WebhookEventStoreJpaAdapter implements WebhookReceivedStore {

    private final WebhookEventJpaRepository repository;

    public WebhookEventStoreJpaAdapter(WebhookEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public WebhookEvent storeWebhook(WebhookEvent webhook) {
        return repository.save(webhook);
    }

    @Override
    public WebhookEvent storeWebhook(GithubEventDto eventDto, JsonNode rawBody) {
        throw new ApplicationException("Webhook event store method not implemented yet");
    }

    @Override
    public boolean hasAlreadyBeenStored(String repositoryFullName, String webhookId) {
        return repository.findFirstByRepositoryFullNameAndWebhookIdOrderByIdDesc(repositoryFullName, webhookId);
    }

    @Override
    public WebhookEvent getLatestWebhook(String repositoryFullName) {
        return repository.findFirstByRepositoryFullNameOrderByIdDesc(repositoryFullName);
    }

}
