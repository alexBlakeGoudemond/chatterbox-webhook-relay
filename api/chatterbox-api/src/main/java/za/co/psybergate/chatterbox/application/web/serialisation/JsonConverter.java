package za.co.psybergate.chatterbox.application.web.serialisation;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;

import java.nio.file.Path;

public interface JsonConverter {

    JsonNode getAsJson(String jsonString);

    String readPayload(String pathToFile);

    String readPayload(Path pathToFile);

    String getRepositoryName(JsonNode rawBody);

}
