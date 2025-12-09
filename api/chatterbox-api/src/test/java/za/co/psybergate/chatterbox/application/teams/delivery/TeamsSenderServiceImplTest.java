package za.co.psybergate.chatterbox.application.teams.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.utility.JsonConverter;

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

        teamsSenderService.send(eventDto);
    }

    private String getValidJsonString() {
        String pathToFile = "src/test/resources/payload/github-payload-valid.json";
        return jsonConverter.readPayload(pathToFile);
    }
}