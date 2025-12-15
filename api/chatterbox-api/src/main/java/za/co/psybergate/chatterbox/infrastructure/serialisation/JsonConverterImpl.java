package za.co.psybergate.chatterbox.infrastructure.serialisation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.exception.InternalServerException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class JsonConverterImpl implements JsonConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JsonNode getAsJson(String jsonString) throws InternalServerException {
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Unable to convert String into JSON", e);
        }
    }

    @Override
    public String readPayload(String pathToFile) {
        try {
            return Files.readString(Paths.get(pathToFile));
        } catch (IOException e) {
            throw new InternalServerException("Could not read github payload file", e);
        }
    }

    @Override
    public String getRepositoryName(JsonNode rawBody) throws InternalServerException {
        String repositoryName = rawBody.path("repository").path("full_name").asText(null);
        if (repositoryName == null) {
            throw new InternalServerException("Unable to parse 'repository.full_name' from raw rawBody");
        }
        return repositoryName;
    }

}
