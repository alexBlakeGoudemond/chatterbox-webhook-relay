package za.co.psybergate.chatterbox.application.domain.discord.model;

import lombok.Data;

// TODO BlakeGoudemond 2026/01/27 | impl of super class: AcceptedChannel
@Data
public class DiscordAcceptedChannel {

    private String channelName;

    private String webhookUrl;

}
