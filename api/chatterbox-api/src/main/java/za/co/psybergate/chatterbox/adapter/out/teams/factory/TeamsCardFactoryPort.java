package za.co.psybergate.chatterbox.adapter.out.teams.factory;

import za.co.psybergate.chatterbox.adapter.out.teams.model.TeamsAdaptiveCardDefinition;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

import java.util.Map;

public interface TeamsCardFactoryPort {

    /// From a given [Map] of property values, create and populate the
    /// internal Teams payload representation
    TeamsAdaptiveCardDefinition buildCard(Map<String, String> values);

    /// From a given [OutboundEvent] create a [Map] and leverage [TeamsCardFactoryPort#buildCard(Map)]
    /// to create the internal Teams payload representation
    TeamsAdaptiveCardDefinition buildCard(OutboundEvent outboundEvent);

    String getAsTeamsPayloadString(OutboundEvent outboundEvent) throws ApplicationException;

}
