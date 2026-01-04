# ApplicationRunner
need an ApplicationRunner or an EventListener that listens for the ApplicationReadyEvent.
When the service starts, this component:
Looks at the github_polled_event table to find the latest fetched_at timestamp for each repository.
Calls your GithubPollingService.getRecentUpdates(...) using that timestamp as the fromDate.
Persists the missing events.
This ensures that the moment the service is healthy, it bridges the gap between the last time it was alive and "now."
  

```java
@Component
@RequiredArgsConstructor
public class CatchUpRunner implements ApplicationRunner {
    private final GithubPollingService pollingService;
    private final GithubPolledStore polledStore;
    private final ApplicationEventPublisher publisher;

    @Override
    public void run(ApplicationArguments args) {
        // 1. Get repos from config
        // 2. find last fetched date from polledStore
        // 3. poll for changes and save
        // 4. For each new event saved, publisher.publishEvent(new EventSaved(id));
    }
}
```

# EventListener

For ongoing operations while the service is running, I agree that Event Listeners are much cleaner than a high-frequency Cron.
For Webhooks: The GithubWebhookController saves to DB and publishes a WebhookReceivedEvent.
For Polling: A low-frequency Cron (e.g., every 5-10 mins) triggers a poll, saves new events, and publishes a PolledEventReceivedEvent.
The EventProcessorImpl then listens for these events and handles the Teams delivery. This keeps your Controller/Poller logic strictly about ingesting and your Processor strictly about delivering.

```java
@Component
@RequiredArgsConstructor
public class DeliveryEventListener {
    private final EventProcessor eventProcessor;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEventSaved(EventSaved event) {
        // Marry the persistence with delivery here
        eventProcessor.processSpecificEvent(event.getId());
    }
}
```

# RetryCronJob

Its only job would be to look for events with EventStatus.PROCESSED_FAILURE and attempt to re-deliver them. This handles the case where MS Teams was down, but your app was up.

