package za.co.psybergate.chatterbox.adapter.in.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.application.common.logging.MdcContext;
import za.co.psybergate.chatterbox.common.logging.mdc.Slf4jMdcContext;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxSecurityWebhookGithubProperties;
import za.co.psybergate.chatterbox.common.exception.InternalServerException;
import za.co.psybergate.chatterbox.common.exception.InvalidSignatureException;
import za.co.psybergate.chatterbox.common.security.PayloadCryptor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Component
public class WebhookFilter implements Filter {

    private final ChatterboxSecurityWebhookGithubProperties securityWebhookGithubProperties;

    private final WebhookLogger webhookLogger;

    private final PayloadCryptor payloadCryptor;

    private final WebhookRuntimeMetrics webhookRuntimeMetrics;

    private final MdcContext mdcContext;

    public WebhookFilter(WebhookLogger webhookLogger,
                         PayloadCryptor payloadCryptor,
                         WebhookRuntimeMetrics webhookRuntimeMetrics,
                         ChatterboxSecurityWebhookGithubProperties securityWebhookGithubProperties,
                         MdcContext mdcContext) {
        this.webhookLogger = webhookLogger;
        this.payloadCryptor = payloadCryptor;
        this.webhookRuntimeMetrics = webhookRuntimeMetrics;
        this.securityWebhookGithubProperties = securityWebhookGithubProperties;
        this.mdcContext = mdcContext;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        mdcContext.initialize();

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
        webhookLogger.logCompletion(ms);

        mdcContext.clear();
    }

    private void assertValidSignature(CachedBodyHttpServletRequest wrappedRequest,
                                      String event,
                                      String delivery,
                                      String signature256) throws InvalidSignatureException {
        byte[] bodyBytes = getBodyAsBytes(wrappedRequest);
        String encoding = getCharacterEncoding(wrappedRequest);
        String rawBody = getRawBody(bodyBytes, encoding);

        webhookLogger.logReceivedWebhookEvent(event, delivery);

        if (signature256 == null) {
            webhookLogger.logMissingSignature();
            webhookRuntimeMetrics.recordSignatureFailure(event);
            throw new InvalidSignatureException("Missing X-Hub-Signature-256");
        }

        String expected = payloadCryptor.encryptUsingSHA256(securityWebhookGithubProperties.getSecret(), rawBody);
        if (!payloadCryptor.isIdentical(expected, signature256)) {
            webhookLogger.logInvalidSignature(expected, signature256);
            webhookRuntimeMetrics.recordSignatureFailure(event);
            throw new InvalidSignatureException("Invalid X-Hub-Signature-256 - does not match rawBody");
        }

        webhookLogger.logValidSignature();
    }

    private String getRawBody(byte[] bodyBytes, String encoding) throws InternalServerException {
        try {
            return new String(bodyBytes, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new InternalServerException("Unexpected issue encountered when converting Byte[] into String", e);
        }
    }

    private String getCharacterEncoding(CachedBodyHttpServletRequest wrappedRequest) {
        String encoding = wrappedRequest.getCharacterEncoding();
        if (encoding == null) {
            encoding = StandardCharsets.UTF_8.name();
        }
        return encoding;
    }

    private byte[] getBodyAsBytes(CachedBodyHttpServletRequest wrappedRequest) throws InternalServerException {
        try {
            return wrappedRequest.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new InternalServerException("Unexpected issue when reading requestBody as Byte[]", e);
        }
    }

}
