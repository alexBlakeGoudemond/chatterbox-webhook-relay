package za.co.psybergate.chatterbox.adapter.out.discord.factory;

import za.co.psybergate.chatterbox.adapter.out.discord.model.DiscordEmbeddedObjectDefinition;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

import java.util.Map;

public interface DiscordEmbeddedObjectFactoryPort {

    /// From a given [Map] of property values, create and populate the
    /// internal Discord payload representation
    DiscordEmbeddedObjectDefinition buildEmbeddedObjectDefinition(Map<String, String> values);

    /// From a given [OutboundEvent] create a [Map] and leverage [DiscordEmbeddedObjectFactoryPort#buildEmbeddedObjectDefinition(Map))]
    /// to create the internal Discord payload representation
    DiscordEmbeddedObjectDefinition buildEmbeddedObjectDefinition(OutboundEvent outboundEvent);

    String getAsDiscordPayloadString(OutboundEvent eventDto) throws ApplicationException;

}
