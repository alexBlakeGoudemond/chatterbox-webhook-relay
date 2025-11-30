package za.co.psybergate.chatterbox.infrastructure.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import za.co.psybergate.chatterbox.application.core.exception.UnauthorizedException;
import za.co.psybergate.chatterbox.application.core.utility.EncryptionUtilities;
import za.co.psybergate.chatterbox.application.web.metric.WebhookMetrics;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class WebhookFilter implements Filter {

    @Value("${webhook.github.secret}")
    private String webhookSecret;

    private final WebhookLogger webhookValidationLogger;

    private final EncryptionUtilities encryptionUtilities;

    private final WebhookMetrics webhookMetrics;

    public WebhookFilter(WebhookLogger webhookValidationLogger, EncryptionUtilities encryptionUtilities, WebhookMetrics webhookMetrics) {
        this.webhookValidationLogger = webhookValidationLogger;
        this.encryptionUtilities = encryptionUtilities;
        this.webhookMetrics = webhookMetrics;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        String threadExecutionId = UUID.randomUUID().toString();
        MDC.put("threadExecutionId", threadExecutionId);

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpRequest);

        String event = wrappedRequest.getHeader("X-GitHub-Event");
        String delivery = wrappedRequest.getHeader("X-GitHub-Delivery");
        String signature256 = wrappedRequest.getHeader("X-Hub-Signature-256");

        byte[] bodyBytes = wrappedRequest.getInputStream().readAllBytes();
        String encoding = wrappedRequest.getCharacterEncoding();
        if (encoding == null) {
            encoding = StandardCharsets.UTF_8.name();
        }
        String rawBody = new String(bodyBytes, encoding);

        webhookValidationLogger.logReceivedWebhookEvent(event, delivery);

        if (signature256 == null) {
            webhookValidationLogger.logMissingSignature();
            webhookMetrics.recordSignatureFailure(event);
            throw new UnauthorizedException("Missing X-Hub-Signature-256");
        }

        String expected = encryptionUtilities.encryptUsingSHA256(webhookSecret, rawBody);
        if (!encryptionUtilities.isIdentical(expected, signature256)) {
            webhookValidationLogger.logInvalidSignature(expected, signature256);
            webhookMetrics.recordSignatureFailure(event);
            throw new UnauthorizedException("Invalid X-Hub-Signature-256 - does not match body");
        }

        webhookValidationLogger.logValidSignature();

        long start = System.currentTimeMillis();
        chain.doFilter(wrappedRequest, response);
        long ms = System.currentTimeMillis() - start;

        webhookMetrics.recordProcessingSuccess(event);

        webhookValidationLogger.logCompletion(ms);
        MDC.clear();
    }

}
