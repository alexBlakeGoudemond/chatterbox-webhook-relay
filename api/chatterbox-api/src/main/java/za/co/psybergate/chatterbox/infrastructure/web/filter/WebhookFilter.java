package za.co.psybergate.chatterbox.infrastructure.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.infrastructure.exception.ApplicationException;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnauthorizedException;
import za.co.psybergate.chatterbox.domain.utility.EncryptionUtilities;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static za.co.psybergate.chatterbox.infrastructure.logging.MDC_KEYS.THREAD_EXECUTION_ID;

@Component
public class WebhookFilter implements Filter {

    @Value("${webhook.github.secret}")
    private String webhookSecret;

    private final WebhookLogger webhookValidationLogger;

    private final EncryptionUtilities encryptionUtilities;

    private final WebhookRuntimeMetrics webhookRuntimeMetrics;

    public WebhookFilter(WebhookLogger webhookValidationLogger, EncryptionUtilities encryptionUtilities, WebhookRuntimeMetrics webhookRuntimeMetrics) {
        this.webhookValidationLogger = webhookValidationLogger;
        this.encryptionUtilities = encryptionUtilities;
        this.webhookRuntimeMetrics = webhookRuntimeMetrics;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        String threadExecutionId = UUID.randomUUID().toString();
        MDC.put(THREAD_EXECUTION_ID.value(), threadExecutionId);

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpRequest);

        String event = wrappedRequest.getHeader("X-GitHub-Event");
        String delivery = wrappedRequest.getHeader("X-GitHub-Delivery");
        String signature256 = wrappedRequest.getHeader("X-Hub-Signature-256");

        assertValidSignature(wrappedRequest, event, delivery, signature256);

        long start = System.currentTimeMillis();
        chain.doFilter(wrappedRequest, response);
        long ms = System.currentTimeMillis() - start;

        webhookRuntimeMetrics.recordProcessingSuccess(event);
        webhookValidationLogger.logCompletion(ms);
        MDC.clear();
    }

    private void assertValidSignature(CachedBodyHttpServletRequest wrappedRequest,
                                      String event,
                                      String delivery,
                                      String signature256) throws ApplicationException {
        byte[] bodyBytes = getBodyAsBytes(wrappedRequest);
        String encoding = getCharacterEncoding(wrappedRequest);
        String rawBody = getRawBody(bodyBytes, encoding);

        webhookValidationLogger.logReceivedWebhookEvent(event, delivery);

        if (signature256 == null) {
            webhookValidationLogger.logMissingSignature();
            webhookRuntimeMetrics.recordSignatureFailure(event);
            throw new UnauthorizedException("Missing X-Hub-Signature-256");
        }

        String expected = encryptionUtilities.encryptUsingSHA256(webhookSecret, rawBody);
        if (!encryptionUtilities.isIdentical(expected, signature256)) {
            webhookValidationLogger.logInvalidSignature(expected, signature256);
            webhookRuntimeMetrics.recordSignatureFailure(event);
            throw new UnauthorizedException("Invalid X-Hub-Signature-256 - does not match body");
        }

        webhookValidationLogger.logValidSignature();
    }

    private String getRawBody(byte[] bodyBytes, String encoding) throws ApplicationException {
        try {
            return new String(bodyBytes, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new ApplicationException("Unexpected issue encountered when converting Byte[] into String", e);
        }
    }

    private String getCharacterEncoding(CachedBodyHttpServletRequest wrappedRequest) {
        String encoding = wrappedRequest.getCharacterEncoding();
        if (encoding == null) {
            encoding = StandardCharsets.UTF_8.name();
        }
        return encoding;
    }

    private byte[] getBodyAsBytes(CachedBodyHttpServletRequest wrappedRequest) throws ApplicationException {
        try {
            return wrappedRequest.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new InternalServerException("Unexpected issue when reading requestBody as Byte[]", e);
        }
    }

}
