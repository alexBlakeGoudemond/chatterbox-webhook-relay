package za.co.psybergate.chatterbox.application.webhook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.webhook.factory.TeamsCardFactory;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamsSenderService {

    //    private final WebClient webClient;

    private final TeamsCardFactory teamsCardFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void process(GithubEventDto eventDto) {
        String teamsPayload = convertToTeamsPayload(eventDto);

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

    /**
     * Sends the mapped GithubEventDto as an Adaptive Card to a Teams webhook URL.
     */
//    public Mono<Void> send(String teamsWebhookUrl, GithubEventDto dto) {
//
//        var cardPayload = buildAdaptiveCardPayload(dto);
//
//        log.info("Sending card to Microsoft Teams webhook: {}", teamsWebhookUrl);
//        log.debug("Teams Adaptive Card Payload: {}", cardPayload);

//        return webClient
//                .post()
//                .uri(teamsWebhookUrl)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(cardPayload)
//                .retrieve()
//                .bodyToMono(String.class)
//                .doOnNext(response -> log.debug("Teams webhook response: {}", response))
//                .then()
//                .doOnSuccess(v -> log.info("Successfully sent Teams message for event {}", dto.eventType()))
//                .doOnError(err -> log.error("Failed to send Teams message", err));
//    }

}
