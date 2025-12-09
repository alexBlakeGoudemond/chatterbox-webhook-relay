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
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;
import za.co.psybergate.chatterbox.infrastructure.exception.ApplicationException;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamsSenderServiceImpl implements TeamsSenderService {

//    private final WebClient webClient;

    private final TeamsCardFactoryImpl teamsCardFactory;

//    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // TODO BlakeGoudemond 2025/12/09 | use logger component
    @Override
    public void process(GithubEventDto eventDto) {
        String teamsPayload = convertToTeamsPayload(eventDto);
        System.out.println("teamsPayload = " + teamsPayload);
//        Mono<Void> send = send(eventDto);
        // TODO BlakeGoudemond 2025/12/09 | what to do with mono?
    }

    /**
     * Sends the mapped GithubEventDto as an Adaptive Card to a Teams webhook URL.
     */
    @Override
    public void send(GithubEventDto dto) {
        String teamsDestination = dto.teamsDestination();
        log.info("Sending card to Microsoft Teams webhook: {}", teamsDestination);
        String jsonString = convertToTeamsPayload(dto);
        log.debug("Teams Adaptive Card Payload: {}", jsonString);

        try (CloseableHttpClient client = HttpClients.custom()
                .disableCookieManagement()
                .setUserAgent("") // empty to avoid breaking signature
                .build()) {

            HttpPost post = new HttpPost(teamsDestination);
            post.setEntity(new StringEntity(jsonString, StandardCharsets.UTF_8));
            post.setHeader("Content-Type", "application/json");

            client.execute(post, response -> {
                System.out.println("Status: " + response.getCode());
                return null;
            });
        } catch (IOException e) {
            throw new ApplicationException("Unexpected issue when sending POST Request to Teams", e);
        }
    }

    private String convertToTeamsPayload(GithubEventDto eventDto) {
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
