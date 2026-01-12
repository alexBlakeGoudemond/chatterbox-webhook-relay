package za.co.psybergate.chatterbox.application.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventDeliveryLog;

import java.util.List;

public interface GithubPolledStore {

    List<GithubPolledEvent> getUnprocessedEvents(String repositoryFullName);

    GithubPolledEvent storeEvent(GithubPolledEvent event);

    GithubPolledEvent storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    List<GithubPolledEvent> getLatestProcessedEvents(String repositoryFullName);

    GithubPolledEventDeliveryLog storeSuccessfulDelivery(GithubPolledEventDeliveryLog polledEventDeliveryLog);

    GithubPolledEventDeliveryLog storeSuccessfulDelivery(GithubPolledEvent polledEvent, String destinationName, String destinationUrl);

    GithubPolledEventDeliveryLog storeUnsuccessfulDelivery(GithubPolledEvent polledEvent, String destinationName, String destinationUrl);

    void setProcessedStatus(GithubPolledEvent polledEvent, EventStatus eventStatus);

    GithubPolledEvent getEvent(Long id);

    List<GithubPolledEventDeliveryLog> getDeliveryLogs(Long id);

    GithubPolledEvent getMostRecentPolledEvent(String repositoryFullName);

}
