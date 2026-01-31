package za.co.psybergate.chatterbox.application.port.out.discord.delivery;

import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;
import za.co.psybergate.chatterbox.adapter.out.http.model.HttpResponseDto;

public interface DiscordSenderPort {

    HttpResponseDto process(GithubEventDto dto, String discordDestination);

}
