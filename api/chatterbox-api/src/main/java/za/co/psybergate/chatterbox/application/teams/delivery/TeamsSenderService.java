package za.co.psybergate.chatterbox.application.teams.delivery;

import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.domain.delivery.model.HttpResponseDto;

public interface TeamsSenderService {

    HttpResponseDto process(GithubEventDto dto, String teamsDestination);


}
