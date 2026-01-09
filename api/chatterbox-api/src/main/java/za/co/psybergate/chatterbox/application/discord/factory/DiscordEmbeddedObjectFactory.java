package za.co.psybergate.chatterbox.application.discord.factory;

import org.apache.hc.core5.http.ClassicHttpResponse;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDeliveryDiscordProperties;

import java.util.Map;

public interface DiscordEmbeddedObjectFactory {

    /// From a given [Map] of property values, create and populate the
    /// [ChatterboxDeliveryDiscordProperties.EmbeddedObjectDefinition]
    ChatterboxDeliveryDiscordProperties.EmbeddedObjectDefinition buildEmbeddedObjectDefinition(Map<String, String> values);

    /// From a given [GithubEventDto] create a [Map] and leverage [DiscordEmbeddedObjectFactory#buildEmbeddedObjectDefinition(Map))]
    /// to create a [ChatterboxDeliveryDiscordProperties.EmbeddedObjectDefinition]
    ChatterboxDeliveryDiscordProperties.EmbeddedObjectDefinition buildEmbeddedObjectDefinition(GithubEventDto dto);

    String getAsDiscordPayloadString(GithubEventDto eventDto) throws ApplicationException;

    HttpResponseDto getHttpResponseDto(ClassicHttpResponse response);

}
