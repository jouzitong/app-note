package org.zzt.note.server.word.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.zzt.note.server.word.entity.meta.ExampleSentenceMetaInfo;

/**
 * ExampleSentenceMetaInfo JSON 转换器
 *
 * @author zhouzhitong
 * @since 2026/3/28
 */
@Converter
public class ExampleSentenceMetaInfoJsonConverter implements AttributeConverter<ExampleSentenceMetaInfo, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ExampleSentenceMetaInfo attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize ExampleSentenceMetaInfo.", e);
        }
    }

    @Override
    public ExampleSentenceMetaInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ExampleSentenceMetaInfo();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, ExampleSentenceMetaInfo.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize ExampleSentenceMetaInfo.", e);
        }
    }
}

