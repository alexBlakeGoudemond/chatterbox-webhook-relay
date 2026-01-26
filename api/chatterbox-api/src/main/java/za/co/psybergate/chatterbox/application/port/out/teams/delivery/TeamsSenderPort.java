package za.co.psybergate.chatterbox.application.port.out.teams.delivery;

import za.co.psybergate.chatterbox.domain.delivery.model.HttpResponseDto;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;

public interface TeamsSenderPort {

    HttpResponseDto process(GithubEventDto dto, String teamsDestination);


}
