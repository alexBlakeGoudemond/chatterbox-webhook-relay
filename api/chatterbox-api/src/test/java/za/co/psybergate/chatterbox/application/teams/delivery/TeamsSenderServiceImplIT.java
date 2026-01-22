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
import za.co.psybergate.chatterbox.application.usecase.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.application.port.out.teams.factory.TeamsCardFactory;
import za.co.psybergate.chatterbox.application.usecase.template.TemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.application.usecase.web.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.application.usecase.webhook.mapper.GithubEventMapper;
import za.co.psybergate.chatterbox.application.usecase.webhook.mapper.GithubEventMapperImpl;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.delivery.model.HttpResponseDto;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.adapter.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.infrastructure.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.infrastructure.in.web.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.out.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.infrastructure.out.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.out.webhook.resolution.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;
import za.co.psybergate.chatterbox.test.helper.TestConfigurationResolver;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        TeamsSenderServiceImpl.class,
        JsonFileReader.class,
        JsonConverterImpl.class,
        GithubEventMapperImpl.class,
        WebhookConfigurationResolverImpl.class,
        TeamsCardFactoryImpl.class,
        TemplateSubstitutorImpl.class,
        InfrastructurePropertiesConfig.class,
        TestConfigurationResolver.class,
        WebhookConfigurationResolverImpl.class,
        WebhookLoggerImpl.class,
        HttpResponseHandler.class,
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
    private GithubEventMapper eventExtractor;

    @Autowired
    private TeamsSenderServiceImpl teamsSenderServiceImpl;

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
    @DisplayName("TeamsSenderService can process DTO")
    @Test
    public void givenGithubEventDto_WhenTeamsSenderServiceProcessesDto_ThenSuccess() {
        GithubEventDto eventDto = getGithubEventDto();
        String teamsDestinationUrl = configurationResolver.getTeamsDestinationUrl(eventDto);

        HttpResponseDto httpResponseDto = teamsSenderServiceImpl.process(eventDto, teamsDestinationUrl);
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

        HttpResponseDto httpResponseDto = teamsSenderServiceImpl.executeHttpPostRequest(httpPost);
        assertNotNull(httpResponseDto);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), httpResponseDto.httpStatus());
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
        return eventExtractor.map(EventType.PUSH, jsonNode);
    }

}