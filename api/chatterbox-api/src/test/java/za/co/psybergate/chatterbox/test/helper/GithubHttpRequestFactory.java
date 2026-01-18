package za.co.psybergate.chatterbox.test.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxApiProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSecurityWebhookGithubProperties;
import za.co.psybergate.chatterbox.infrastructure.in.web.security.PayloadCryptor;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class GithubHttpRequestFactory {

    @Autowired
    private ChatterboxSecurityWebhookGithubProperties securityWebhookGithubProperties;

    @Autowired
    private ChatterboxApiProperties chatterboxApiProperties;

    @Autowired
    private PayloadCryptor payloadCryptor;

    public MockHttpServletRequestBuilder getHttpRequestValid(String payload) {
        String encryptedSignature = payloadCryptor.encryptUsingSHA256(webhookSecret(), payload);
        return post(apiPrefix() + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push")
                .header("X-Hub-Signature-256", encryptedSignature);
    }

    public MockHttpServletRequestBuilder getHttpRequestUnknownEvent(String payload, String unknownEventType) {
        String encryptedSignature = payloadCryptor.encryptUsingSHA256(webhookSecret(), payload);
        return post(apiPrefix() + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", unknownEventType)
                .header("X-Hub-Signature-256", encryptedSignature);
    }

    public MockHttpServletRequestBuilder getHttpRequestValidNoEncoding(String payload) {
        String encryptedSignature = payloadCryptor.encryptUsingSHA256(webhookSecret(), payload);
        return post(apiPrefix() + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push")
                .header("X-Hub-Signature-256", encryptedSignature);
    }

    public MockHttpServletRequestBuilder getHttpRequestInvalidSignature(String payload) {
        String unencryptedPayloadSecret = webhookSecret();
        return post(apiPrefix() + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push")
                .header("X-Hub-Signature-256", unencryptedPayloadSecret);
    }

    public MockHttpServletRequestBuilder getHttpRequestNoSignature(String payload) {
        return post(apiPrefix() + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push");
    }

    private String apiPrefix() {
        return chatterboxApiProperties.getPrefix();
    }

    private String webhookSecret() {
        return securityWebhookGithubProperties.getSecret();
    }

}
