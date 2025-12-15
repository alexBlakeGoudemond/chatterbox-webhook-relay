package za.co.psybergate.chatterbox.application.teams.factory;

import org.apache.hc.core5.http.ClassicHttpResponse;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDeliveryTeamsProperties;
import za.co.psybergate.chatterbox.application.exception.InternalServerException;

import java.util.Map;

public interface TeamsCardFactory {

    ChatterboxDeliveryTeamsProperties.TeamsAdaptiveCardDefinition buildCard(Map<String, String> values);

    ChatterboxDeliveryTeamsProperties.TeamsAdaptiveCardDefinition buildCard(GithubEventDto dto);

    String getAsTeamsPayloadString(GithubEventDto eventDto) throws InternalServerException;

    HttpResponseDto getHttpResponseDto(ClassicHttpResponse response) throws InternalServerException;

}
