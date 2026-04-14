package org.zzt.note.server.word.article.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.zzt.note.server.word.article.entity.meta.ArticleMetaInfo;

/**
 * ArticleMetaInfo JSON 转换器
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
@Converter
public class ArticleMetaInfoJsonConverter implements AttributeConverter<ArticleMetaInfo, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ArticleMetaInfo attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize ArticleMetaInfo.", e);
        }
    }

    @Override
    public ArticleMetaInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArticleMetaInfo();
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, ArticleMetaInfo.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize ArticleMetaInfo.", e);
        }
    }
}
