package za.co.psybergate.chatterbox.domain.utility;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.infrastructure.exception.ApplicationException;

public interface ConversionUtilities {

    JsonNode getAsJson(String jsonString) throws ApplicationException;

}
