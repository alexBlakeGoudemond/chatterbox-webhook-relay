package za.co.psybergate.chatterbox.infrastructure.logging;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PayloadCachingRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public PayloadCachingRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = request.getInputStream().readAllBytes();
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new DelegatingServletInputStream(bais);
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

}
