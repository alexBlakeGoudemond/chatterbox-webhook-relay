package za.co.psybergate.chatterbox.application.domain.event.model;

public record OutboundEvent(
        Long id,
        String sourceId,
        String type,
        String title,
        String repository,
        String actor,
        String url,
        String displayText,
        String extra,
        String payload
) {

}

