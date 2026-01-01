package za.co.psybergate.chatterbox.application.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;

public interface GithubPolledStore {

    GithubPolledEvent storeEvent(GithubPolledEvent event);

    GithubPolledEvent storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    boolean hasAlreadyBeenStored(String repositoryFullName, EventType eventType, String sourceId);

    GithubPolledEvent getLatestEvent(String repositoryFullName);

    void logDelivery(GithubPolledEvent polledEvent);
}
