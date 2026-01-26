package za.co.psybergate.chatterbox.test.helper;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JsonConverter;

import java.nio.file.Path;

@Component
public class JsonFileReader {

    private static final String BASE_DIRECTORY = "src/test/resources/payload";

    @Autowired
    private JsonConverter jsonConverter;

    public String getGithubPayloadValidAsString() {
        Path pathToFile = Path.of(BASE_DIRECTORY, "github-payload-valid.json");
        return jsonConverter.readPayload(pathToFile);
    }

    public JsonNode getGithubPayloadValid() {
        return jsonConverter.getAsJson(getGithubPayloadValidAsString());
    }

    public JsonNode getTeamsPayloadValid() {
        Path pathToFile = Path.of(BASE_DIRECTORY, "teams-payload-valid.json");
        return jsonConverter.getAsJson(jsonConverter.readPayload(pathToFile));
    }

    public JsonNode getGithubPayloadUnknownEvent() {
        Path pathToFile = Path.of(BASE_DIRECTORY, "github-payload-invalid-unknown-event-type.json");
        return jsonConverter.getAsJson(jsonConverter.readPayload(pathToFile));
    }

    public JsonNode getGithubPayloadMissingProperties() {
        Path pathToFile = Path.of(BASE_DIRECTORY, "github-payload-invalid-missing-properties.json");
        return jsonConverter.getAsJson(jsonConverter.readPayload(pathToFile));
    }

    public JsonNode getGithubPayloadInvalidEventTypeAndRepositoryName() {
        Path pathToFile = Path.of(BASE_DIRECTORY, "github-payload-invalid-contains-event-type-and-repository-name.json");
        return jsonConverter.getAsJson(jsonConverter.readPayload(pathToFile));
    }

    public JsonNode getGithubPayloadNoDisplayText() {
        Path pathToFile = Path.of(BASE_DIRECTORY, "github-payload-invalid-no-url-display-text.json");
        return jsonConverter.getAsJson(jsonConverter.readPayload(pathToFile));
    }

    public JsonNode getGithubPayloadLongDisplayText() {
        Path pathToFile = Path.of(BASE_DIRECTORY, "github-payload-valid-long-url-display-text.json");
        return jsonConverter.getAsJson(jsonConverter.readPayload(pathToFile));
    }

    public JsonNode getDiscordPayloadValid() {
        Path pathToFile = Path.of(BASE_DIRECTORY, "discord-payload-valid.json");
        return jsonConverter.getAsJson(jsonConverter.readPayload(pathToFile));
    }

}
