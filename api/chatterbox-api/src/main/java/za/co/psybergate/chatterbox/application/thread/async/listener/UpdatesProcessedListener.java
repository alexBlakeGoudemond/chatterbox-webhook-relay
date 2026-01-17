package za.co.psybergate.chatterbox.application.thread.async.listener;

import za.co.psybergate.chatterbox.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.domain.event.notification.WebhookEventProcessed;

// TODO BlakeGoudemond 2026/01/17 | test?
public interface UpdatesProcessedListener {

    void onPolledEventsProcessed(PolledEventsProcessed polledEventsProcessed);

    void onWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed);

}
