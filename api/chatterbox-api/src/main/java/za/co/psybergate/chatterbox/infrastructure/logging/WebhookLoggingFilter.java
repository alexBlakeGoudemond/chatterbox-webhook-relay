package za.co.psybergate.chatterbox.infrastructure.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class WebhookLoggingFilter implements Filter {

    @Value("${chatterbox.webhook.log-payload:false}")
    private boolean logPayload;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String threadExecutionId = UUID.randomUUID().toString();
        MDC.put("threadExecutionId", threadExecutionId);

        String event = httpRequest.getHeader("X-GitHub-Event");
        String delivery = httpRequest.getHeader("X-GitHub-Delivery");

        log.info("[Webhook] Received event={} delivery={} threadExecutionId={}", event, delivery, threadExecutionId);

        if (logPayload && httpRequest instanceof PayloadCachingRequestWrapper wrapper) {
            log.debug("[Webhook] Payload threadExecutionId={} body={}", threadExecutionId, wrapper.getBodyAsString());
        }

        long start = System.currentTimeMillis();
        chain.doFilter(httpRequest, response);
        long ms = System.currentTimeMillis() - start;

        log.info("[Webhook] Completed threadExecutionId={} in {}ms", threadExecutionId, ms);

        MDC.clear();
    }

}
