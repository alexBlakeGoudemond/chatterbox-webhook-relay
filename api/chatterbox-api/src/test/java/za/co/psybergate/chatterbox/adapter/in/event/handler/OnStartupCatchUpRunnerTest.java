package za.co.psybergate.chatterbox.adapter.in.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationEventPublisher;
import za.co.psybergate.chatterbox.application.common.logging.MdcContext;
import za.co.psybergate.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.WebhookOrchestratorPort;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnStartupCatchUpRunnerTest {

    @Mock
    private WebhookOrchestratorPort webhookService;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private MdcContext mdcContext;

    private OnStartupCatchUpRunner onStartupCatchUpRunner;

    @BeforeEach
    void setUp() {
        onStartupCatchUpRunner = new OnStartupCatchUpRunner(webhookService, publisher, mdcContext);
    }

    @Test
    @DisplayName("Should delegate to webhookService when getAllRepositories is called")
    void givenRepositories_WhenGetAllRepositories_ThenWebhookServiceCalled() {
        List<String> expectedRepositories = List.of("repo1", "repo2");
        when(webhookService.getAllRepositories()).thenReturn(expectedRepositories);
        List<String> actualRepositories = onStartupCatchUpRunner.getAllRepositories();
        assertEquals(expectedRepositories, actualRepositories);
        verify(webhookService).getAllRepositories();
    }

    @Test
    @DisplayName("Missed Event causes published event")
    void givenMissedEvent_WhenProcessMissedEvents_ThenPublisherSubmitsEvent() {
        List<String> repositories = List.of("repo1");
        when(webhookService.findMostRecentWebhookAndCheckForUpdatesSince("repo1")).thenReturn(true);
        onStartupCatchUpRunner.processMissedEvents(repositories);
        verify(publisher).publishEvent(any(PolledEventsProcessed.class));
    }

    @Test
    @DisplayName("No Missed Events causes no published event")
    void givenNoMissedEvents_WhenProcessMissedEvents_ThenPublisherDoesNothing() {
        List<String> repositories = List.of("repo1");
        when(webhookService.findMostRecentWebhookAndCheckForUpdatesSince("repo1")).thenReturn(false);
        onStartupCatchUpRunner.processMissedEvents(repositories);
        verify(publisher, never()).publishEvent(any(PolledEventsProcessed.class));
    }

    @Test
    @DisplayName("Empty repository causes no published event")
    void givenNoRepository_WhenProcessMissedEvents_ThenPublisherDoesNothing() {
        onStartupCatchUpRunner.processMissedEvents(Collections.emptyList());
        verify(webhookService, never()).findMostRecentWebhookAndCheckForUpdatesSince(anyString());
        verify(publisher, never()).publishEvent(any(PolledEventsProcessed.class));
    }

    @Test
    @DisplayName("Should call getAllRepositories and processMissedEvents when run is called")
    void whenRunnerStartsUp_ThenServiceCalled_AndPublisherSendsEvent() throws Exception {
        List<String> repositories = List.of("repo1");
        when(webhookService.getAllRepositories()).thenReturn(repositories);
        when(webhookService.findMostRecentWebhookAndCheckForUpdatesSince("repo1")).thenReturn(true);
        ApplicationArguments args = mock(ApplicationArguments.class);
        onStartupCatchUpRunner.run(args);
        verify(webhookService).getAllRepositories();
        verify(webhookService).findMostRecentWebhookAndCheckForUpdatesSince("repo1");
        verify(publisher).publishEvent(any(PolledEventsProcessed.class));
    }

}
