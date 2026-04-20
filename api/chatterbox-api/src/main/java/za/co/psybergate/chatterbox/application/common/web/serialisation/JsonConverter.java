package com.webhook.relay.chatterbox.application.common.web.serialisation;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;

public interface JsonConverter {

    JsonNode getAsJson(String jsonString);

    String readPayload(String pathToFile);

    String readPayload(Path pathToFile);

    String getRepositoryName(JsonNode rawBody);

}
