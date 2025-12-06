package za.co.psybergate.chatterbox.domain.utility;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

public interface ConversionUtilities {

    JsonNode getAsJson(String jsonString) throws InternalServerException;

}
