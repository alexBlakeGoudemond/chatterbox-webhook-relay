package za.co.psybergate.chatterbox.application.port.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.event.model.GithubPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.domain.event.model.GithubPolledEventDto;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;

import java.util.List;

public interface GithubPolledEventStore {

    List<GithubPolledEventDto> getUnprocessedEvents(String repositoryFullName);

    GithubPolledEventDto storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    List<GithubPolledEventDto> getLatestProcessedEvents(String repositoryFullName);

    GithubPolledEventDeliveryDto storeSuccessfulDelivery(GithubPolledEventDto polledEvent, String destinationName, String destinationUrl);

    GithubPolledEventDeliveryDto storeUnsuccessfulDelivery(GithubPolledEventDto polledEvent, String destinationName, String destinationUrl);

    void setProcessedStatus(GithubPolledEventDto polledEvent, EventStatus eventStatus);

    GithubPolledEventDto getEvent(Long id);

    List<GithubPolledEventDeliveryDto> getDeliveryLogs(Long id);

    GithubPolledEventDto getMostRecentPolledEvent(String repositoryFullName);

}
