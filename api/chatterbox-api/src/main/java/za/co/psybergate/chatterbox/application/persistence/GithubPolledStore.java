package za.co.psybergate.chatterbox.application.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventDeliveryLog;

import java.util.List;

public interface GithubPolledStore {

    GithubPolledEvent storeEvent(GithubPolledEvent event);

    GithubPolledEvent storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    boolean hasAlreadyBeenStored(String repositoryFullName, EventType eventType, String sourceId);

    List<GithubPolledEvent> getLatestEvents(String repositoryFullName);

    GithubPolledEventDeliveryLog storeDelivery(GithubPolledEventDeliveryLog polledEventDeliveryLog);

    GithubPolledEventDeliveryLog storeDelivery(GithubPolledEvent polledEvent, String exampleDestination, String exampleDestinationUrk);

    void setProcessedStatus(GithubPolledEvent polledEvent, EventStatus eventStatus);

    void setProcessedStatus(GithubPolledEvent polledEvent, EventStatus eventStatus, String responseDtoErrorResponse);

    GithubPolledEvent getEvent(Long id);

    List<GithubPolledEventDeliveryLog> getDeliveryLogs(Long id);

    List<GithubPolledEvent> getLatestPolledEvents(String repositoryFullName);

    GithubPolledEvent getMostRecentPolledEvent(String repositoryFullName);
}
