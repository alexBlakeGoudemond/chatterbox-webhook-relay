package za.co.psybergate.chatterbox.application.webhook.routing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

@Component
@RequiredArgsConstructor
public class WebhookConfigurationResolverImpl implements WebhookConfigurationResolver {

    private final ChatterboxConfigurationProperties configurationProperties;

    @Override
    public ChatterboxConfigurationProperties.PayloadMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException {
        var payloadMapping = configurationProperties.getGithubIncomingMappings().get(eventType);
        if (payloadMapping != null) {
            return payloadMapping;
        }
        throw new UnrecognizedRequestException(String.format("Unsupported event type '%s'", eventType));
    }

    @Override
    public String getDestinationUrl(String repositoryName) throws UnrecognizedRequestException {
        for (ChatterboxConfigurationProperties.AcceptedRepository acceptedRepository : configurationProperties.getGithubRepositoriesAccepted()) {
            if (acceptedRepository.getName().equals(repositoryName)) {
                return configurationProperties.getTeamsDestinationUrl(acceptedRepository.getDestinationChannel());
            }
        }
        throw new UnrecognizedRequestException("Unable to find the destination for " + repositoryName);
    }

}
