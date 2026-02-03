package za.co.psybergate.chatterbox.application.domain.event.model;

/**
 * Represents a raw event payload from a source (e.g., GitHub).
 * This class wraps the raw data to avoid leaking infrastructure-specific types (like Jackson's JsonNode) 
 * into the domain layer, while providing a way to access the underlying data in the application layer.
 */
public record RawEventPayload(Object payload) {

    public static RawEventPayload of(Object payload) {
        return new RawEventPayload(payload);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(Class<T> type) {
        if (type.isInstance(payload)) {
            return (T) payload;
        }
        throw new IllegalArgumentException("Payload is not of type " + type.getName());
    }

}
