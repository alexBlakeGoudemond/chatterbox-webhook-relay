package za.co.psybergate.chatterbox.application.discord.delivery;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.discord.factory.DiscordEmbeddedObjectFactory;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class DiscordSenderServiceImpl implements DiscordSenderService {

    private final DiscordEmbeddedObjectFactory discordEmbeddedObjectFactory;

    private final WebhookLogger webhookLogger;

    @Override
    public HttpResponseDto process(GithubEventDto dto, String discordDestination) {
        webhookLogger.logSendingDtoToDiscord(dto, discordDestination);
        String jsonString = discordEmbeddedObjectFactory.getAsDiscordPayloadString(dto);
        HttpPost httpPost = getHttpPost(discordDestination, jsonString);
        return executeHttpPostRequest(httpPost);
    }

    @Override
    public HttpResponseDto executeHttpPostRequest(HttpPost httpPost) throws ApplicationException {
        try (CloseableHttpClient client = getCloseableHttpClient()) {
            return client.execute(httpPost, discordEmbeddedObjectFactory::getHttpResponseDto);
        } catch (IOException e) {
            throw new ApplicationException("Unexpected issue when sending POST Request to Teams", e);
        }
    }

    private HttpPost getHttpPost(String discordDestination, String jsonString) {
        HttpPost httpPost = new HttpPost(discordDestination);
        httpPost.setEntity(new StringEntity(jsonString, StandardCharsets.UTF_8));
        httpPost.setHeader("Content-Type", "application/json");
        return httpPost;
    }

    private CloseableHttpClient getCloseableHttpClient() {
        return HttpClients.custom()
                .disableCookieManagement()
                .setUserAgent("")
                .build();
    }

}
