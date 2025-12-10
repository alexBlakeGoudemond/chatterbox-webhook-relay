package za.co.psybergate.chatterbox.application.teams.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.domain.utility.JsonConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles({"live-url"})
public class TeamsSenderServiceImplTest {

    @Autowired
    private JsonConverter jsonConverter;

    @Autowired
    private GithubEventExtractor eventExtractor;

    @Autowired
    private TeamsSenderService teamsSenderService;

    @DisplayName("Teams Sender Service can process DTO")
    @Test
    public void givenGithubEventDto_WhenTeamsSenderServiceProcessesDto_ThenSuccess() {
        GithubEventDto eventDto = getGithubEventDto();

        HttpResponseDto httpResponseDto = teamsSenderService.process(eventDto);
        assertNotNull(httpResponseDto);
        assertEquals(HttpStatus.ACCEPTED.value(), httpResponseDto.httpStatus());
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