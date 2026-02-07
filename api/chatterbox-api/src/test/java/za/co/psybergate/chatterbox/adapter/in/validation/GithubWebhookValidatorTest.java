package za.co.psybergate.chatterbox.adapter.in.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxSourceGithubPayloadProperties;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxSourceGithubRepositoryProperties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubWebhookValidatorTest {

    @Mock
    private ChatterboxSourceGithubPayloadProperties payloadProperties;

    @Mock
    private ChatterboxSourceGithubRepositoryProperties repositoryProperties;

    @Mock
    private WebhookLogger webhookLogger;

    private GithubWebhookValidator underTest;

    @BeforeEach
    void setUp() {
        underTest = new GithubWebhookValidator(payloadProperties, repositoryProperties, webhookLogger);
    }

    @Test
    @DisplayName("Should pass when event type is accepted")
    void givenValidEventType_whenAssertAcceptedEvent_ThenPayloadPropertiesIsCalled() {
        String eventType = "push";
        when(payloadProperties.containsEvent(eventType)).thenReturn(true);
        assertDoesNotThrow(() -> underTest.assertAcceptedEvent(eventType));
    }

    @Test
    @DisplayName("Should throw UnrecognizedRequestException when event type is not accepted")
    void givenInvalidEventType_WhenAssertAcceptedEvent_ThenPayloadPropertiesIsCalled() {
        String eventType = "unknown";
        when(payloadProperties.containsEvent(eventType)).thenReturn(false);
        assertThrows(UnrecognizedRequestException.class, () -> underTest.assertAcceptedEvent(eventType));
        verify(webhookLogger).logUnknownEventType(eventType);
    }

    @Test
    @DisplayName("Should pass when repository name is accepted")
    void givenValidRepositoryName_WhenAssertAcceptedRepository_ThenRepositoryPropertiesIsCalled() {
        String repositoryName = "owner/repo";
        when(repositoryProperties.acceptsRepository(repositoryName)).thenReturn(true);
        assertDoesNotThrow(() -> underTest.assertAcceptedRepository(repositoryName));
    }

    @Test
    @DisplayName("Should throw UnrecognizedRequestException when repository name is not accepted")
    void whenInvalidRepositoryName_WhenAssertAcceptedRepository_ThenFailure() {
        String repositoryName = "unknown/repo";
        when(repositoryProperties.acceptsRepository(repositoryName)).thenReturn(false);
        assertThrows(UnrecognizedRequestException.class, () -> underTest.assertAcceptedRepository(repositoryName));
        verify(webhookLogger).logUnrecognizedRepository(repositoryName);
    }

    @Test
    @DisplayName("Should pass when owner and repository name are accepted")
    void givenValidCredentials_WhenAssertAcceptedRepository_ThenSuccess() {
        String owner = "owner";
        String repo = "repo";
        String fullName = "owner/repo";
        when(repositoryProperties.acceptsRepository(fullName)).thenReturn(true);
        assertDoesNotThrow(() -> underTest.assertAcceptedRepository(owner, repo));
    }

    @Test
    @DisplayName("Should throw UnrecognizedRequestException when owner and repository name are not accepted")
    void givenInvalidCredentials_WhenAssertAcceptedRepository_ThenSuccess() {
        String owner = "unknown";
        String repo = "repo";
        String fullName = "unknown/repo";
        when(repositoryProperties.acceptsRepository(fullName)).thenReturn(false);
        assertThrows(UnrecognizedRequestException.class, () -> underTest.assertAcceptedRepository(owner, repo));
        verify(webhookLogger).logUnrecognizedRepository(fullName);
    }

}
