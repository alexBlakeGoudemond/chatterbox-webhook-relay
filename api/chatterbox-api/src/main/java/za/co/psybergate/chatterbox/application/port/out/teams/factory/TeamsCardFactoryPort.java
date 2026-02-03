package za.co.psybergate.chatterbox.application.port.out.teams.factory;

import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

import java.util.Map;

// TODO BlakeGoudemond 2026/02/03 | I dont like Object here, it is a hack. Likely this must be in adapter
public interface TeamsCardFactoryPort {

    /// From a given [Map] of property values, create and populate the
    /// internal Teams payload representation
    Object buildCard(Map<String, String> values);

    /// From a given [OutboundEvent] create a [Map] and leverage [TeamsCardFactoryPort#buildCard(Map)]
    /// to create the internal Teams payload representation
    Object buildCard(OutboundEvent outboundEvent);

    String getAsTeamsPayloadString(OutboundEvent outboundEvent) throws ApplicationException;

}
