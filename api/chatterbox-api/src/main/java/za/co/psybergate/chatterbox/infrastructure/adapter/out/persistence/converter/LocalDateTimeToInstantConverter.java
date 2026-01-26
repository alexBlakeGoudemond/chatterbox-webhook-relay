package za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Converter
public class LocalDateTimeToInstantConverter implements AttributeConverter<LocalDateTime, Instant> {

    @Override
    public Instant convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null
                ? null
                : attribute.toInstant(ZoneOffset.UTC);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Instant dbData) {
        return dbData == null
                ? null
                : LocalDateTime.ofInstant(dbData, ZoneOffset.UTC);
    }

}
