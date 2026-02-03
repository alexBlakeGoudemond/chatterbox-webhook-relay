package za.co.psybergate.chatterbox.application.domain.event.model;

import lombok.Getter;

/// The EventStatuses defined here are defined primarily for the Database
@Getter
public enum WebhookEventStatus {

    RECEIVED,
    PROCESSING,
    PROCESSED_SUCCESS,
    PROCESSED_FAILURE

}
