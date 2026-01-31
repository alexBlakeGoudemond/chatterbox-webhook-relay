package za.co.psybergate.chatterbox.application.port.out.teams.delivery;

import za.co.psybergate.chatterbox.application.domain.delivery.model.HttpResponseDto;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;

public interface TeamsSenderPort {

    HttpResponseDto process(GithubEventDto dto, String teamsDestination);


}
