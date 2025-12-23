package za.co.psybergate.chatterbox.application.github.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
public class GithubWebClientPollingServiceImpl {

    private final WebClient githubClient;

    public GithubWebClientPollingServiceImpl(@Qualifier("githubClient") WebClient webClient) {
        this.githubClient = webClient;
    }

    public JsonNode getCommitsSince(String owner, String repositoryName, LocalDateTime lastReceivedUpdate) {
        return githubClient.get()
                .uri(uri -> uri
                        .path("/repos/{owner}/{repo}/commits")
                        .queryParam("since", lastReceivedUpdate.toString())
                        .build(owner, repositoryName))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

}
