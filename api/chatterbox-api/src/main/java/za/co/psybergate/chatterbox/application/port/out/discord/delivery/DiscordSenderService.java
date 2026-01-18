package za.co.psybergate.chatterbox.application.port.out.discord.delivery;

import za.co.psybergate.chatterbox.domain.delivery.model.HttpResponseDto;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;

public interface DiscordSenderService {

    HttpResponseDto process(GithubEventDto dto, String discordDestination);

}
