package za.co.psybergate.chatterbox.application.teams.delivery;

import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

public interface TeamsSenderService {

    void process(GithubEventDto eventDto);

    void send(GithubEventDto dto);

}
