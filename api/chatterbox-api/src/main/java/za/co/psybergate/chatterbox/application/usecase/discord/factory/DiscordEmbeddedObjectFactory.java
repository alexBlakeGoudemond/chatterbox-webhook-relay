package za.co.psybergate.chatterbox.application.usecase.discord.factory;

import za.co.psybergate.chatterbox.application.usecase.discord.model.DiscordEmbeddedObjectDefinition;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;

import java.util.Map;

public interface DiscordEmbeddedObjectFactory {

    /// From a given [Map] of property values, create and populate the
    /// [DiscordEmbeddedObjectDefinition]
    DiscordEmbeddedObjectDefinition buildEmbeddedObjectDefinition(Map<String, String> values);

    /// From a given [GithubEventDto] create a [Map] and leverage [DiscordEmbeddedObjectFactory#buildEmbeddedObjectDefinition(Map))]
    /// to create a [DiscordEmbeddedObjectDefinition]
    DiscordEmbeddedObjectDefinition buildEmbeddedObjectDefinition(GithubEventDto dto);

    String getAsDiscordPayloadString(GithubEventDto eventDto) throws ApplicationException;

}
