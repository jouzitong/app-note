package org.zzt.note.server.practice.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.zzt.note.server.practice.entity.meta.QuestionMetaInfo;

/**
 * QuestionMetaInfo JSON 转换器
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Converter
public class QuestionMetaInfoJsonConverter implements AttributeConverter<QuestionMetaInfo, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(QuestionMetaInfo attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize QuestionMetaInfo.", e);
        }
    }

    @Override
    public QuestionMetaInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new QuestionMetaInfo();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, QuestionMetaInfo.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize QuestionMetaInfo.", e);
        }
    }
}

