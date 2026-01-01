package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;

@Component
@Transactional
public class WebhookEventStoreJpaAdapter implements WebhookReceivedStore {

    private final WebhookEventJpaRepository repository;

    private final WebhookEventLogJpaRepository logRepository;

    public WebhookEventStoreJpaAdapter(WebhookEventJpaRepository repository,
                                       WebhookEventLogJpaRepository logRepository) {
        this.repository = repository;
        this.logRepository = logRepository;
    }

    @Override
    public WebhookEvent storeWebhook(WebhookEvent webhook) {
        return repository.save(webhook);
    }

    @Override
    public WebhookEvent storeWebhook(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        WebhookEvent webhook = new WebhookEvent(uniqueId, eventDto, rawBody);
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

    @Override
    public void logDelivery(GithubPolledEvent polledEvent){
        throw new ApplicationException("Not implemented");
    }

}
