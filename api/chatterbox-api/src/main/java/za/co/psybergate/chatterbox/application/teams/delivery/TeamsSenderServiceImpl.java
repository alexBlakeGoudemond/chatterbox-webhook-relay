package za.co.psybergate.chatterbox.application.teams.delivery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.teams.factory.TeamsCardFactory;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamsSenderServiceImpl implements TeamsSenderService {

    private final TeamsCardFactory teamsCardFactory;

    // TODO BlakeGoudemond 2025/12/11 | modify that one test to use Mockito to simulate success instead of mvn clean install publishing to teams
    @Override
    public HttpResponseDto process(GithubEventDto dto) throws InternalServerException {
        String teamsDestination = dto.teamsDestination();
        String jsonString = teamsCardFactory.getAsTeamsPayloadString(dto);
        HttpPost httpPost = getHttpPost(teamsDestination, jsonString);
        return executeHttpPostRequest(httpPost);
    }

    @Override
    public HttpResponseDto executeHttpPostRequest(HttpPost httpPost) throws InternalServerException {
        try (CloseableHttpClient client = getCloseableHttpClient()) {
            return client.execute(httpPost, teamsCardFactory::getHttpResponseDto);
        } catch (IOException e) {
            throw new InternalServerException("Unexpected issue when sending POST Request to Teams", e);
        }
    }

    private HttpPost getHttpPost(String teamsDestination, String jsonString) {
        HttpPost httpPost = new HttpPost(teamsDestination);
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
