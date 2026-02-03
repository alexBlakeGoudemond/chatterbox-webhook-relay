package za.co.psybergate.chatterbox.application.port.out.discord.factory;

import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

import java.util.Map;

// TODO BlakeGoudemond 2026/02/03 | I dont like Object here, it is a hack. Likely this must be in adapter
public interface DiscordEmbeddedObjectFactoryPort {

    /// From a given [Map] of property values, create and populate the
    /// internal Discord payload representation
    Object buildEmbeddedObjectDefinition(Map<String, String> values);

    /// From a given [OutboundEvent] create a [Map] and leverage [DiscordEmbeddedObjectFactoryPort#buildEmbeddedObjectDefinition(Map))]
    /// to create the internal Discord payload representation
    Object buildEmbeddedObjectDefinition(OutboundEvent outboundEvent);

    String getAsDiscordPayloadString(OutboundEvent eventDto) throws ApplicationException;

}
