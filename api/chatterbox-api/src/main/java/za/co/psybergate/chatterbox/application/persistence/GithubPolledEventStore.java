package za.co.psybergate.chatterbox.application.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.persistence.dto.GithubPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.application.persistence.dto.GithubPolledEventDto;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

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
