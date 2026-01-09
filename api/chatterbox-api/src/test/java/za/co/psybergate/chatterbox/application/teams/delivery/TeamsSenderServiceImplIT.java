package za.co.psybergate.chatterbox.application.teams.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.teams.factory.TeamsCardFactory;
import za.co.psybergate.chatterbox.application.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.infrastructure.template.TemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;
import za.co.psybergate.chatterbox.test.helper.TestConfigurationResolver;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        TeamsSenderServiceImpl.class,
        JsonFileReader.class,
        JsonConverterImpl.class,
        GithubEventExtractorImpl.class,
        WebhookConfigurationResolverImpl.class,
        TeamsSenderServiceImpl.class,
        TeamsCardFactoryImpl.class,
        TemplateSubstitutorImpl.class,
        ApplicationConfig.class,
        TestConfigurationResolver.class,
        WebhookConfigurationResolverImpl.class,
        WebhookLogger.class,
        HttpResponseHandler.class
})
@ActiveProfiles({"live-url"})
public class TeamsSenderServiceImplIT {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubEventExtractor eventExtractor;

    @Autowired
    private TeamsSenderService teamsSenderService;

    @Autowired
    private TeamsCardFactory teamsCardFactory;

    @Autowired
    private TestConfigurationResolver configurationResolver;

    /// Send an actual test to the MS Teams API and assert that the HttpResponse
    /// information is as-expected.
    ///
    /// This test is annotated with a Tag that `maven-surefire-plugin` is made aware of.
    /// This means that running `mvn clean install` will NOT include this by default
    @Tag("live-integration")
    @DisplayName("Teams Sender Service can process DTO")
    @Test
    public void givenGithubEventDto_WhenTeamsSenderServiceProcessesDto_ThenSuccess() {
        GithubEventDto eventDto = getGithubEventDto();
        String teamsDestinationUrl = configurationResolver.getTeamsDestinationUrl(eventDto);

        HttpResponseDto httpResponseDto = teamsSenderService.process(eventDto, teamsDestinationUrl);
        assertNotNull(httpResponseDto);
        assertEquals(HttpStatus.ACCEPTED.value(), httpResponseDto.httpStatus());
    }

    @DisplayName("Bad HttpPost yields 401")
    @Test
    public void givenGithubEventDto_AndBadHttpPost_WhenExecuteHttp_ThenUnauthorised() {
        GithubEventDto eventDto = getGithubEventDto();
        String teamsDestinationUrl = configurationResolver.getTeamsDestinationUrl(eventDto);

        String jsonString = teamsCardFactory.getAsTeamsPayloadString(eventDto);
        HttpPost httpPost = getHttpPostWithAuthorizationHeaders(teamsDestinationUrl, jsonString);

        HttpResponseDto httpResponseDto = teamsSenderService.executeHttpPostRequest(httpPost);
        assertNotNull(httpResponseDto);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), httpResponseDto.httpStatus());
        assertEquals("DirectApiRequestHasMoreThanOneAuthorization", httpResponseDto.jsonNode().get("error").get("code").asText());
    }

    private HttpPost getHttpPostWithAuthorizationHeaders(String teamsDestination, String jsonString) {
        HttpPost httpPost = new HttpPost(teamsDestination);
        httpPost.setEntity(new StringEntity(jsonString, StandardCharsets.UTF_8));
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer broken-test-token");
        return httpPost;
    }

    private GithubEventDto getGithubEventDto() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        return eventExtractor.extract(EventType.PUSH, jsonNode);
    }

}