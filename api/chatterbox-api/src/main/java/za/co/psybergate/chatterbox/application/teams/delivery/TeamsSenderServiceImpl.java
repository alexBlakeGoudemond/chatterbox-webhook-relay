package za.co.psybergate.chatterbox.application.teams.delivery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamsSenderServiceImpl implements TeamsSenderService {

    private final TeamsCardFactoryImpl teamsCardFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public HttpResponseDto process(GithubEventDto dto) throws InternalServerException {
        String teamsDestination = dto.teamsDestination();
        String jsonString = convertToTeamsPayload(dto);
        return executeHttpPostRequest(teamsDestination, jsonString);
    }

    @Override
    public HttpResponseDto executeHttpPostRequest(String teamsDestination, String jsonString) throws InternalServerException {
        HttpPost httpPost = getHttpPost(teamsDestination, jsonString);
        try (CloseableHttpClient client = getCloseableHttpClient()) {
            return client.execute(httpPost, response -> {
                int status = response.getCode();
                String body = null;
                if (response.getEntity() != null) {
                    body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                }
                return new HttpResponseDto(status, body);
            });
        } catch (IOException e) {
            throw new InternalServerException("Unexpected issue when sending POST Request to Teams", e);
        }
    }

    // TODO BlakeGoudemond 2025/12/10 | Mock this in a test and set the following to assert a 401 post.setHeader("Authorization", "Bearer broken-test-token");
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

    private String convertToTeamsPayload(GithubEventDto eventDto) throws InternalServerException {
        TeamsAdaptiveCardTemplate teamsAdaptiveCardTemplate = teamsCardFactory.buildCard(eventDto);
        String teamsPayload;
        try {
            teamsPayload = objectMapper.writeValueAsString(teamsAdaptiveCardTemplate);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Unexpected issue when converting EventDto to Json String", e);
        }
        return teamsPayload;
    }

}
