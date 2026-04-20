package com.webhook.relay.chatterbox.adapter.out.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.webhook.relay.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import com.webhook.relay.chatterbox.application.domain.configuration.EventPayloadMapping;
import com.webhook.relay.chatterbox.application.domain.configuration.EventPayloadMapping.IncomingMappingFieldKeys;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.RawEventPayload;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.domain.exception.DomainException;
import com.webhook.relay.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;
import com.webhook.relay.chatterbox.test.helper.JsonFileReader;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static com.webhook.relay.chatterbox.application.domain.configuration.EventPayloadMapping.IncomingMappingFieldKeys.*;

@ExtendWith(MockitoExtension.class)
class GithubWebhookEventMapperTest {

    private final JacksonJsonConverter jsonConverter = new JacksonJsonConverter();

    private final JsonFileReader jsonFileReader = new JsonFileReader();

    @Mock
    private WebhookConfigurationResolverPort configurationResolver;

    private GithubWebhookEventMapper eventExtractor;

    private Validator validator;

    @BeforeEach
    void setUp() throws Exception {
        eventExtractor = new GithubWebhookEventMapper(configurationResolver);

        // Inject jsonConverter into jsonFileReader manually since it's a unit test
        Field jsonConverterField = JsonFileReader.class.getDeclaredField("jsonConverter");
        jsonConverterField.setAccessible(true);
        jsonConverterField.set(jsonFileReader, jsonConverter);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private void setupPayloadMapping(WebhookEventType type) {
        Map<IncomingMappingFieldKeys, String> fields = Map.of(
                REPOSITORYNAME, "repository.full_name",
                URLDISPLAYTEXT, "head_commit.message",
                SENDERNAME, "sender.login",
                URL, "compare",
                EXTRADETAIL, "action"
        );
        EventPayloadMapping mapping = EventPayloadMapping.builder()
                .displayName("Push Event")
                .fields(fields)
                .build();
        when(configurationResolver.getPayloadMapping(type)).thenReturn(mapping);
    }

    @DisplayName("Extractor maps to DTO")
    @Test
    void givenJsonString_WhenMap_ThenSuccess() {
        setupPayloadMapping(WebhookEventType.PUSH);
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));

        assertNotNull(outboundEvent);
        validate(outboundEvent);

        assertEquals(WebhookEventType.PUSH.name(), outboundEvent.type());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", outboundEvent.repository());
        assertEquals("psyAlexBlakeGoudemond", outboundEvent.actor());
        assertEquals("https://github.com/psyAlexBlakeGoudemond/chatterbox/blob/develop/api/chatterbox-api/chattering_teeth.gif", outboundEvent.url());
        assertEquals("Test message Is here!", outboundEvent.displayText());
    }

    @DisplayName("Unknown Event: Exception")
    @Test
    void givenJsonString_WithUnknownEvent_WhenMap_ThenException() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadUnknownEvent();
        assertThrows(DomainException.class,
                () -> eventExtractor.map("unknownEvent", RawEventPayload.of(jsonNode)));
    }

    @DisplayName("Missing All JSON keys: Exception")
    @Test
    void givenIncompleteJsonString_WhenMap_ThenException() {
        setupPayloadMapping(WebhookEventType.PUSH);
        JsonNode jsonNode = jsonFileReader.getGithubPayloadMissingProperties();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));
        assertThrows(ConstraintViolationException.class, () -> validate(outboundEvent));
    }

    @DisplayName("No UrlDisplayText; then eventType")
    @Test
    void givenJsonString_WithNoUrlDisplayText_WhenMap_ThenUrlDisplayTextIsEventType() {
        setupPayloadMapping(WebhookEventType.PUSH);
        JsonNode jsonNode = jsonFileReader.getGithubPayloadNoDisplayText();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));

        assertNotNull(outboundEvent);
        validate(outboundEvent);

        assertEquals(WebhookEventType.PUSH.name(), outboundEvent.type());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", outboundEvent.repository());
        assertEquals("psyAlexBlakeGoudemond", outboundEvent.actor());
        assertEquals("http://localhost:abcd", outboundEvent.url());
        assertEquals("Push Event", outboundEvent.displayText());
    }

    @DisplayName("Long UrlDisplayText is Truncated")
    @Test
    void givenJsonString_WithLongUrlDisplayText_WhenMap_ThenUrlDisplayTextIsTruncated() {
        setupPayloadMapping(WebhookEventType.PUSH);
        JsonNode jsonNode = jsonFileReader.getGithubPayloadLongDisplayText();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));

        assertNotNull(outboundEvent);
        validate(outboundEvent);

        assertEquals(WebhookEventType.PUSH.name(), outboundEvent.type());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", outboundEvent.repository());
        assertEquals("psyAlexBlakeGoudemond", outboundEvent.actor());
        assertEquals("https://github.com/psyAlexBlakeGoudemond/chatterbox/blob/develop/api/chatterbox-api/chattering_teeth.gif", outboundEvent.url());

        assertFalse(outboundEvent.displayText().contains("\n"));
        assertTrue(outboundEvent.displayText().contains("..."));
    }

    private void validate(OutboundEvent event) {
        var violations = validator.validate(event);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

}