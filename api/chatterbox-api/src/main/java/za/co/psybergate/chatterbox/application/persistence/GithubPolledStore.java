package za.co.psybergate.chatterbox.application.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.event.GithubPolledEventDeliveryRecord;
import za.co.psybergate.chatterbox.domain.event.GithubPolledEventRecord;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventDeliveryLog;

import java.util.List;

public interface GithubPolledStore {

    List<GithubPolledEventRecord> getUnprocessedEvents(String repositoryFullName);

    GithubPolledEventRecord storeEvent(GithubPolledEvent event);

    GithubPolledEventRecord storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    List<GithubPolledEventRecord> getLatestProcessedEvents(String repositoryFullName);

    GithubPolledEventDeliveryRecord storeSuccessfulDelivery(GithubPolledEventDeliveryLog polledEventDeliveryLog);

    GithubPolledEventDeliveryRecord storeSuccessfulDelivery(GithubPolledEventRecord polledEvent, String destinationName, String destinationUrl);

    GithubPolledEventDeliveryRecord storeUnsuccessfulDelivery(GithubPolledEventRecord polledEvent, String destinationName, String destinationUrl);

    void setProcessedStatus(GithubPolledEventRecord polledEvent, EventStatus eventStatus);

    GithubPolledEventRecord getEvent(Long id);

    List<GithubPolledEventDeliveryRecord> getDeliveryLogs(Long id);

    GithubPolledEventRecord getMostRecentPolledEvent(String repositoryFullName);

}
