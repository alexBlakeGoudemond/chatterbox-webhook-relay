package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

@Component
@Transactional
public class WebhookEventStoreJpaAdapter implements WebhookReceivedStore {

    private final WebhookEventJpaRepository repository;

    private final WebhookEventLogJpaRepository logRepository;

    private final WebhookLogger webhookLogger;

    public WebhookEventStoreJpaAdapter(WebhookEventJpaRepository repository,
                                       WebhookEventLogJpaRepository logRepository,
                                       WebhookLogger webhookLogger) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.webhookLogger = webhookLogger;
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
    public WebhookEvent storeWebhook(WebhookEvent webhook) {
        webhookLogger.logStoringEvent(webhook);
        WebhookEvent save = repository.save(webhook);
        return save;
    }

    @Override
    public WebhookEvent storeWebhook(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        WebhookEvent webhook = new WebhookEvent(uniqueId, eventDto, rawBody);
        return storeWebhook(webhook);
    }

    @Override
    public WebhookEventLog storeDelivery(WebhookEventLog webhookEventLog) {
        webhookLogger.logDeliveringEvent(webhookEventLog);
        return logRepository.save(webhookEventLog);
    }

    @Override
    public WebhookEventLog storeDelivery(WebhookEvent webhookEvent, String destinationName, String destinationUrl) {
        WebhookEventLog webhookEventLog = new WebhookEventLog(webhookEvent, destinationName, destinationUrl);
        return storeDelivery(webhookEventLog);
    }

}
