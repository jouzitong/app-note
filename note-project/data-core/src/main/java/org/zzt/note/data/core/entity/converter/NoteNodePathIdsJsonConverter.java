package org.zzt.note.data.core.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * NoteNode pathIds JSON 转换器
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Converter
public class NoteNodePathIdsJsonConverter implements AttributeConverter<List<Long>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize NoteNode pathIds.", e);
        }
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize NoteNode pathIds.", e);
        }
    }
}
