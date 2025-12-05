package za.co.psybergate.chatterbox.domain.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.infrastructure.exception.ApplicationException;

@Component
public class ConversionUtilitiesImpl implements ConversionUtilities {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JsonNode getAsJson(String jsonString) throws ApplicationException {
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Unable to convert String into JSON", e);
        }
    }

}
