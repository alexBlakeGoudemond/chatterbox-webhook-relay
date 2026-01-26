package za.co.psybergate.chatterbox.application.port.out.teams.factory;

import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.domain.teams.model.TeamsAdaptiveCardDefinition;

import java.util.Map;

public interface TeamsCardFactoryPort {

    /// From a given [Map] of property values, create and populate the
    /// [TeamsAdaptiveCardDefinition]
    TeamsAdaptiveCardDefinition buildCard(Map<String, String> values);

    /// From a given [GithubEventDto] create a [Map] and leverage [TeamsCardFactoryPort#buildCard(Map)]
    /// to create a [TeamsAdaptiveCardDefinition]
    TeamsAdaptiveCardDefinition buildCard(GithubEventDto dto);

    String getAsTeamsPayloadString(GithubEventDto eventDto) throws ApplicationException;

}
