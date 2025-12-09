package za.co.psybergate.chatterbox.application.teams.delivery;

import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

public interface TeamsSenderService {

    HttpResponseDto process(GithubEventDto dto) throws InternalServerException;

    HttpResponseDto executeHttpPostRequest(String teamsDestination, String jsonString) throws InternalServerException;

}
