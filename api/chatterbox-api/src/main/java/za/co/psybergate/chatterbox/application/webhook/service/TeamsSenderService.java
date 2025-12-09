package za.co.psybergate.chatterbox.application.webhook.service;

import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

public interface TeamsSenderService {

    void process(GithubEventDto eventDto);

}
