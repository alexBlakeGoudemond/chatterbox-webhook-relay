package za.co.psybergate.application.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubWebhookController.class)
public class GithubWebhookControllerTest {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("api/webhook/github works")
    @Test
    void whenPostToGithubWebhook_WithJson_ThenAccepted() throws Exception {
        mockMvc.perform(post(apiPrefix + "/webhook/github")
                        .contentType(APPLICATION_JSON)
                        .content("{\"ping\":\"hello\"}"))
                .andExpect(status().isAccepted());
    }

}