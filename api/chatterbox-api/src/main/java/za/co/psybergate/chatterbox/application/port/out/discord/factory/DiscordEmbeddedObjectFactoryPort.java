package za.co.psybergate.chatterbox.application.port.out.discord.factory;

import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.adapter.out.discord.model.DiscordEmbeddedObjectDefinition;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

import java.util.Map;

public interface DiscordEmbeddedObjectFactoryPort {

    /// From a given [Map] of property values, create and populate the
    /// [DiscordEmbeddedObjectDefinition]
    DiscordEmbeddedObjectDefinition buildEmbeddedObjectDefinition(Map<String, String> values);

    /// From a given [OutboundEvent] create a [Map] and leverage [DiscordEmbeddedObjectFactoryPort#buildEmbeddedObjectDefinition(Map))]
    /// to create a [DiscordEmbeddedObjectDefinition]
    DiscordEmbeddedObjectDefinition buildEmbeddedObjectDefinition(OutboundEvent outboundEvent);

    String getAsDiscordPayloadString(OutboundEvent eventDto) throws ApplicationException;

}
