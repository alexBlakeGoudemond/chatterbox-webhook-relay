package za.co.psybergate.chatterbox.application.port.out.teams.delivery;

import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;
import za.co.psybergate.chatterbox.adapter.out.http.model.HttpResponseDto;

public interface TeamsSenderPort {

    HttpResponseDto process(GithubEventDto dto, String teamsDestination);


}
