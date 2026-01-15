package za.co.psybergate.chatterbox.application.teams.delivery;

import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;

public interface TeamsSenderService {

    HttpResponseDto process(GithubEventDto dto, String teamsDestination);


}
