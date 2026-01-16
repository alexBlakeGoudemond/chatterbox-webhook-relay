package za.co.psybergate.chatterbox.application.teams.factory;

import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.teams.model.TeamsAdaptiveCardDefinition;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

import java.util.Map;

public interface TeamsCardFactory {

    /// From a given [Map] of property values, create and populate the
    /// [TeamsAdaptiveCardDefinition]
    TeamsAdaptiveCardDefinition buildCard(Map<String, String> values);

    /// From a given [GithubEventDto] create a [Map] and leverage [TeamsCardFactory#buildCard(Map)]
    /// to create a [TeamsAdaptiveCardDefinition]
    TeamsAdaptiveCardDefinition buildCard(GithubEventDto dto);

    String getAsTeamsPayloadString(GithubEventDto eventDto) throws ApplicationException;

}
