package za.co.psybergate.chatterbox.application.teams.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.domain.utility.JsonConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class TeamsSenderServiceImplTest {

    @Autowired
    private JsonConverter jsonConverter;

    @Autowired
    private GithubEventExtractor eventExtractor;

    @Autowired
    private TeamsSenderService teamsSenderService;

    @Test
    public void teamsSenderServiceTest() {
        JsonNode jsonNode = jsonConverter.getAsJson(getValidJsonString());
        GithubEventDto eventDto = eventExtractor.extract("push", jsonNode);

        HttpResponseDto httpResponseDto = teamsSenderService.process(eventDto);
        assertNotNull(httpResponseDto);
        assertEquals(HttpStatus.ACCEPTED.value(), httpResponseDto.httpStatus());
    }

    private String getValidJsonString() {
        String pathToFile = "src/test/resources/payload/github-payload-valid.json";
        return jsonConverter.readPayload(pathToFile);
    }
}