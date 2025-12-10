package za.co.psybergate.chatterbox.application.teams.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import za.co.psybergate.chatterbox.application.teams.factory.TeamsCardFactory;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.domain.utility.JsonConverter;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"live-url"})
public class TeamsSenderServiceImplTest {

    @Autowired
    private JsonConverter jsonConverter;

    @Autowired
    private GithubEventExtractor eventExtractor;

    @Autowired
    private TeamsSenderService teamsSenderService;

    @Autowired
    private TeamsCardFactory teamsCardFactory;

    @DisplayName("Teams Sender Service can process DTO")
    @Test
    public void givenGithubEventDto_WhenTeamsSenderServiceProcessesDto_ThenSuccess() {
        GithubEventDto eventDto = getGithubEventDto();

        HttpResponseDto httpResponseDto = teamsSenderService.process(eventDto);
        assertNotNull(httpResponseDto);
        assertEquals(HttpStatus.ACCEPTED.value(), httpResponseDto.httpStatus());
    }

    @DisplayName("Bad HttpPost yields 401")
    @Test
    public void givenGithubEventDto_AndBadHttpPost_WhenExecuteHttp_ThenUnauthorised() {
        GithubEventDto eventDto = getGithubEventDto();

        String teamsDestination = eventDto.teamsDestination();
        String jsonString = teamsCardFactory.getAsTeamsPayloadString(eventDto);
        HttpPost httpPost = getHttpPostWithAuthorizationHeaders(teamsDestination, jsonString);

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
        JsonNode jsonNode = jsonConverter.getAsJson(getValidJsonString());
        return eventExtractor.extract("push", jsonNode);
    }

    private String getValidJsonString() {
        String pathToFile = "src/test/resources/payload/github-payload-valid.json";
        return jsonConverter.readPayload(pathToFile);
    }

}