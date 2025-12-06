package za.co.psybergate.chatterbox.domain.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.infrastructure.exception.ApplicationException;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class ConversionUtilitiesImpl implements ConversionUtilities {

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
            throw new ApplicationException("Could not read github payload file", e);
        }
    }

}
