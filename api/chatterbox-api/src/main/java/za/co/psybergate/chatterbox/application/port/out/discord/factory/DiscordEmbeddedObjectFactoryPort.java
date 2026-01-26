package za.co.psybergate.chatterbox.application.port.out.discord.factory;

import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.domain.discord.model.DiscordEmbeddedObjectDefinition;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubEventDto;

import java.util.Map;

public interface DiscordEmbeddedObjectFactoryPort {

    /// From a given [Map] of property values, create and populate the
    /// [DiscordEmbeddedObjectDefinition]
    DiscordEmbeddedObjectDefinition buildEmbeddedObjectDefinition(Map<String, String> values);

    /// From a given [GithubEventDto] create a [Map] and leverage [DiscordEmbeddedObjectFactoryPort#buildEmbeddedObjectDefinition(Map))]
    /// to create a [DiscordEmbeddedObjectDefinition]
    DiscordEmbeddedObjectDefinition buildEmbeddedObjectDefinition(GithubEventDto dto);

    String getAsDiscordPayloadString(GithubEventDto eventDto) throws ApplicationException;

}
