package za.co.psybergate.chatterbox.infrastructure.serialisation;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;

// TODO BlakeGoudemond 2025/12/11 | place this and Payload in infrastructure
public interface JsonConverter {

    JsonNode getAsJson(String jsonString) throws ApplicationException;

    String readPayload(String pathToFile) throws ApplicationException;

    String getRepositoryName(JsonNode rawBody) throws ApplicationException;

}
