package za.co.psybergate.chatterbox.application.thread.async.listener;

import za.co.psybergate.chatterbox.domain.event.PolledEventsProcessed;
import za.co.psybergate.chatterbox.domain.event.WebhookEventProcessed;

public interface UpdatesProcessedListener {

    void onPolledEventsProcessed(PolledEventsProcessed polledEventsProcessed);

    void onWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed);

}
