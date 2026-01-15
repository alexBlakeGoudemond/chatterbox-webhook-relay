package za.co.psybergate.chatterbox.application.discord.delivery;

import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;

public interface DiscordSenderService {

    HttpResponseDto process(GithubEventDto dto, String discordDestination);

}
