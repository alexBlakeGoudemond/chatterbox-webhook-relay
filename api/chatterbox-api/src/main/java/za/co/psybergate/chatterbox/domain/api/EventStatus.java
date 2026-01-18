package za.co.psybergate.chatterbox.domain.api;

import lombok.Getter;

/// The EventStatuses defined here are defined primarily for the Database
@Getter
public enum EventStatus {

    RECEIVED,
    PROCESSING,
    PROCESSED_SUCCESS,
    PROCESSED_FAILURE

}
