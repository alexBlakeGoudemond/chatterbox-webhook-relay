package za.co.psybergate.chatterbox.application.usecase.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JsonConverter;
import za.co.psybergate.chatterbox.application.domain.event.model.*;
import za.co.psybergate.chatterbox.application.domain.event.notification.WebhookEventProcessed;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventReceived;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventReceived;
import za.co.psybergate.chatterbox.application.port.in.validation.WebhookRequestValidatorPort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import za.co.psybergate.chatterbox.application.port.out.webhook.poll.WebhookPollingPort;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookOrchestratorTest {

    @Mock
    private WebhookRequestValidatorPort webhookRequestValidatorPort;

    @Mock
    private OutboundEventMapperPort eventExtractor;

    @Mock
    private JsonConverter jsonConverter;

    @Mock
    private WebhookPollingPort webhookPollingPort;

    @Mock
    private WebhookEventStorePort webhookEventStorePort;

    @Mock
    private WebhookPolledEventStorePort webhookPolledEventStorePort;

    @Mock
    private WebhookConfigurationResolverPort configurationResolver;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private WebhookLogger webhookLogger;

    private WebhookOrchestrator orchestrator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        orchestrator = new WebhookOrchestrator(
                webhookRequestValidatorPort,
                eventExtractor,
                jsonConverter,
                webhookPollingPort,
                webhookEventStorePort,
                webhookPolledEventStorePort,
                configurationResolver,
                publisher,
                webhookLogger
        );
    }

    @Test
    @DisplayName("Should process webhook successfully")
    void givenReceivedEvent_WhenProcessWebhookSuccessfully_ThenPublisherSubmits() {
        String eventType = "PUSH";
        String deliveryId = "delivery-123";
        String rawBody = "{\"repository\": {\"full_name\": \"org/repo\"}}";
        JsonNode jsonNode = objectMapper.createObjectNode();
        OutboundEvent outboundEvent = createOutboundEvent();
        WebhookEventReceived expectedResponse = createWebhookEventReceived();
        when(jsonConverter.getAsJson(rawBody)).thenReturn(jsonNode);
        when(jsonConverter.getRepositoryName(jsonNode)).thenReturn("org/repo");
        when(eventExtractor.map(eq(eventType), any(RawEventPayload.class))).thenReturn(outboundEvent);
        when(webhookEventStorePort.storeWebhook(eq(deliveryId), eq(outboundEvent), any(RawEventPayload.class)))
                .thenReturn(expectedResponse);
        WebhookEventReceived result = orchestrator.process(eventType, deliveryId, rawBody);

        assertEquals(expectedResponse, result);
        verify(webhookRequestValidatorPort).assertAcceptedRepository("org/repo");
        verify(webhookRequestValidatorPort).assertAcceptedEvent(eventType);
        verify(publisher).publishEvent(any(WebhookEventProcessed.class));
    }

    @Test
    @DisplayName("Should poll for changes with dates successfully")
    void whenPollForChanges_ThenAllPiecesWork() {
        String owner = "owner";
        String repo = "repo";
        String fullName = "owner/repo";
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime until = LocalDateTime.now();
        RepositoryUpdates updates = new RepositoryUpdates(from, until);
        ObjectNode commitNode = objectMapper.createObjectNode();
        commitNode.put("sha", "sha-123");
        updates.add(WebhookEventType.POLL_COMMIT, List.of(RawEventPayload.of(commitNode)));
        OutboundEvent outboundEvent = createOutboundEvent();
        WebhookPolledEventReceived polledEvent = createWebhookPolledEventReceived();
        when(webhookPollingPort.getRecentUpdates(owner, repo, from, until)).thenReturn(updates);
        when(eventExtractor.map(eq("POLL_COMMIT"), any(RawEventPayload.class))).thenReturn(outboundEvent);
        when(webhookPolledEventStorePort.storeEvent(eq("sha-123"), eq(outboundEvent), any(RawEventPayload.class)))
                .thenReturn(polledEvent);
        List<WebhookPolledEventReceived> result = orchestrator.pollForChanges(owner, repo, from, until);

        assertEquals(1, result.size());
        assertEquals(polledEvent, result.getFirst());
        assertEquals(fullName, commitNode.get("full_name").asText());
        verify(webhookRequestValidatorPort).assertAcceptedRepository(owner, repo);
    }

    @Test
    @DisplayName("Should poll for changes with repository full name and received time")
    void pollForChangesWithFullNameSuccessfully() {
        String fullName = "owner/repo";
        LocalDateTime receivedAt = LocalDateTime.now().minusHours(1);
        RepositoryUpdates updates = new RepositoryUpdates(receivedAt, receivedAt); // Simplified for mock
        when(webhookPollingPort.getRecentUpdates(eq("owner"), eq("repo"), eq(receivedAt), any(LocalDateTime.class)))
                .thenReturn(updates);
        orchestrator.pollForChanges(fullName, receivedAt);

        verify(webhookRequestValidatorPort).assertAcceptedRepository("owner", "repo");
        verify(webhookPollingPort).getRecentUpdates(eq("owner"), eq("repo"), eq(receivedAt), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should find most recent webhook and check for updates")
    void findMostRecentWebhookAndCheckForUpdatesSince() {
        String fullName = "owner/repo";
        LocalDateTime polledTime = LocalDateTime.now().minusHours(1);
        WebhookEventReceived latestWebhook = mock(WebhookEventReceived.class);
        WebhookPolledEventReceived latestPolled = mock(WebhookPolledEventReceived.class);
        mock_findMostRecentWebhookAndCheckForUpdatesSince(fullName, polledTime, latestPolled, latestWebhook);
        boolean result = orchestrator.findMostRecentWebhookAndCheckForUpdatesSince(fullName);

        assertTrue(result);
        verify(webhookLogger).logRunnerFoundPreviousWebhook(latestWebhook);
        verify(webhookLogger).logRunnerFoundPreviousPolledEvent(latestPolled);
        verify(webhookPollingPort).getRecentUpdates(eq("owner"), eq("repo"), eq(polledTime), any(LocalDateTime.class));
    }

    private void mock_findMostRecentWebhookAndCheckForUpdatesSince(String fullName, LocalDateTime polledTime, WebhookPolledEventReceived latestPolled, WebhookEventReceived latestWebhook) {
        LocalDateTime webhookTime = LocalDateTime.now().minusHours(2);
        RepositoryUpdates updates = new RepositoryUpdates(polledTime, polledTime);
        ObjectNode node = objectMapper.createObjectNode();
        node.put("sha", "sha-456");
        updates.add(WebhookEventType.POLL_COMMIT, List.of(RawEventPayload.of(node)));

        when(webhookPollingPort.getRecentUpdates(eq("owner"), eq("repo"), eq(polledTime), any(LocalDateTime.class)))
                .thenReturn(updates);
        when(eventExtractor.map(anyString(), any())).thenReturn(createOutboundEvent());
        when(webhookPolledEventStorePort.storeEvent(anyString(), any(), any())).thenReturn(createWebhookPolledEventReceived());
        when(latestWebhook.receivedAt()).thenReturn(webhookTime);
        when(webhookEventStorePort.getMostRecentWebhook(fullName)).thenReturn(latestWebhook);
        when(latestPolled.fetchedAt()).thenReturn(polledTime);
        when(webhookPolledEventStorePort.getMostRecentPolledEvent(fullName)).thenReturn(latestPolled);
    }

    @Test
    @DisplayName("Should return false when no previous webhook found")
    void findMostRecentWebhookReturnsFalseWhenNoneFound() {
        String fullName = "owner/repo";
        when(webhookEventStorePort.getMostRecentWebhook(fullName)).thenThrow(new ApplicationException("Not found"));
        boolean result = orchestrator.findMostRecentWebhookAndCheckForUpdatesSince(fullName);

        assertFalse(result);
        verify(webhookLogger).logRunnerFoundNoPreviousWebhooks(fullName);
        verifyNoInteractions(webhookPolledEventStorePort);
    }

    @Test
    @DisplayName("Should get all repositories")
    void getAllRepositories() {
        List<String> repos = List.of("repo1", "repo2");
        when(configurationResolver.getAllRepositories()).thenReturn(repos);
        List<String> result = orchestrator.getAllRepositories();

        assertEquals(repos, result);
    }

    private OutboundEvent createOutboundEvent() {
        return new OutboundEvent(1L, "sid", "PUSH", "title", "repo", "actor", "url", "text", "extra", "{}");
    }

    private WebhookEventReceived createWebhookEventReceived() {
        return new WebhookEventReceived(
                1L, "repo", "wid", WebhookEventType.PUSH, "title", "sender", "url", "text", "extra", "{}",
                WebhookEventStatus.RECEIVED, null, LocalDateTime.now(), null
        );
    }

    private WebhookPolledEventReceived createWebhookPolledEventReceived() {
        return new WebhookPolledEventReceived(
                1L, "repo", "sid", WebhookEventType.POLL_COMMIT, "title", "sender", "url", "text", "extra", "{}",
                WebhookEventStatus.RECEIVED, null, LocalDateTime.now(), null
        );
    }
}
