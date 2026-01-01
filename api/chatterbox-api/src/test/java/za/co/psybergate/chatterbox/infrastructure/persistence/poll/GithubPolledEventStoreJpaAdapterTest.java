package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.helper.JsonFileReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Transactional
public class GithubPolledEventStoreJpaAdapterTest {

    @Autowired
    private GithubPolledEventStoreJpaAdapter adapter;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubEventExtractor eventExtractor;

    @DisplayName("Can save GithubPolledEvent")
    @Test
    @Rollback
    public void givenPayloadAndPolledEvent_WhenStoreEvent_ThenSuccess() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.extract(EventType.PUSH, jsonNode);

        GithubPolledEvent polledEvent = adapter.storeEvent("abc123", eventDto, jsonNode);
        assertNotNull(polledEvent);
    }

    @DisplayName("Can save GithubPolledEventLog")
    @Test
    @Rollback
    public void givenGithubEvent_WhenStoreDelivery_ThenSuccess() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.extract(EventType.PUSH, jsonNode);
        GithubPolledEvent polledEvent = new GithubPolledEvent("abc123", eventDto, jsonNode);
        polledEvent.setId(1L);
        GithubPolledEventLog polledEventLog = adapter.storeDelivery(polledEvent, "exampleDestination", "exampleDestinationUrl");
        assertNotNull(polledEventLog);
    }

}