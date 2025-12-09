package za.co.psybergate.chatterbox.application.teams.service;

import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

public interface TeamsSenderService {

    void process(GithubEventDto eventDto);

}
