package za.co.psybergate.chatterbox.infrastructure.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.TeamsAdaptiveCardTemplateProperties;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

import java.util.Collections;

@Configuration
@EnableConfigurationProperties({
        ChatterboxConfigurationProperties.class,
        TeamsAdaptiveCardTemplateProperties.class
})
public class ApplicationConfig {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public FilterRegistrationBean<WebhookFilter> applicationWebhookFilter(WebhookFilter webhookFilter) {
        FilterRegistrationBean<WebhookFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(webhookFilter);
        String url = apiPrefix + "/webhook/*";
        registration.addUrlPatterns(url); // only intercept webhook endpoints
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
