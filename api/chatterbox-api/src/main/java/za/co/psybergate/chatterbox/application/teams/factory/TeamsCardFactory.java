package za.co.psybergate.chatterbox.application.teams.factory;

import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;

import java.util.Map;

public interface TeamsCardFactory {

    TeamsAdaptiveCardTemplate buildCard(Map<String, String> values);

    TeamsAdaptiveCardTemplate buildCard(GithubEventDto dto);

}
