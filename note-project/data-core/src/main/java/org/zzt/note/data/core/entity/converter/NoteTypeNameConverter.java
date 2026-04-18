package org.zzt.note.data.core.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;
import org.zzt.note.data.core.type.NoteType;

/**
 * NoteType <-> DB string conversion.
 * <p>
 * Persist enum by {@link NoteType#name()} and support reading by enum name,
 * display name, or numeric code for backward compatibility.
 */
@Converter(autoApply = false)
public class NoteTypeNameConverter implements AttributeConverter<NoteType, String> {

    @Override
    public String convertToDatabaseColumn(NoteType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public NoteType convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return null;
        }

        String value = dbData.trim();

        for (NoteType noteType : NoteType.values()) {
            if (noteType.name().equalsIgnoreCase(value)) {
                return noteType;
            }
            if (noteType.getName().equalsIgnoreCase(value)) {
                return noteType;
            }
            if (String.valueOf(noteType.getCode()).equals(value)) {
                return noteType;
            }
        }

        throw new IllegalArgumentException("Unsupported NoteType value: " + dbData);
    }
}
