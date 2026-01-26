package za.co.psybergate.chatterbox.application.port.out.discord.delivery;

import za.co.psybergate.chatterbox.application.domain.delivery.model.HttpResponseDto;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubEventDto;

public interface DiscordSenderPort {

    HttpResponseDto process(GithubEventDto dto, String discordDestination);

}
