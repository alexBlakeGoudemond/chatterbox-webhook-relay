package za.co.psybergate.chatterbox.infrastructure.adapter.out.discord.delivery;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.port.out.discord.delivery.DiscordSenderPort;
import za.co.psybergate.chatterbox.application.port.out.discord.factory.DiscordEmbeddedObjectFactoryPort;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.delivery.model.HttpResponseDto;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.http.HttpResponseHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class DiscordWebhookSender implements DiscordSenderPort {

    private final DiscordEmbeddedObjectFactoryPort discordEmbeddedObjectFactoryPort;

    private final WebhookLogger webhookLogger;

    private final HttpResponseHandler httpResponseHandler;

    @Override
    public HttpResponseDto process(GithubEventDto dto, String discordDestination) {
        webhookLogger.logSendingDtoToDiscord(dto, discordDestination);
        String jsonString = discordEmbeddedObjectFactoryPort.getAsDiscordPayloadString(dto);
        HttpPost httpPost = getHttpPost(discordDestination, jsonString);
        return executeHttpPostRequest(httpPost);
    }

    /// Executes an HTTP POST request to a Microsoft Teams webhook (or any HTTP endpoint) using a
    /// configured [CloseableHttpClient].
    ///
    /// **What this method does**
    ///
    /// - Builds an [HttpPost] request with a JSON payload and appropriate headers to
    ///   send POST requests without leveraging Spring's RestTemplate / WebClient Stack
    /// - It is important that the [HttpPost] Headers only contain `application/json`
    /// - **The following headers (often created for free) produce a 401 FORBIDDEN**:
    ///     - `Authorization`
    ///     - `Host`
    ///     - `Content-Length`
    ///     - `User-Agent`
    ///     - `Accept`
    /// - Returns the result wrapped in a typed `HttpResponseDto`.
    ///
    /// @return an `HttpResponseDto` containing the HTTP status code and optional rawBody
    /// @throws ApplicationException if an I/O or network-related issue occurs during execution
    public HttpResponseDto executeHttpPostRequest(HttpPost httpPost) throws ApplicationException {
        try (CloseableHttpClient client = getCloseableHttpClient()) {
            return client.execute(httpPost, httpResponseHandler::getHttpResponseDto);
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
