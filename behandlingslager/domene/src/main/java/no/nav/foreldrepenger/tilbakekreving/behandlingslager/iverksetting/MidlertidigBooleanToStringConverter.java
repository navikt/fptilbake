package no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import java.util.Optional;

@Converter
class MidlertidigBooleanToStringConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return null;
        } else {
            return attribute ? "J" : "N";
        }
    }

    @Override
    public Boolean convertToEntityAttribute(String val) {
        return Optional.ofNullable(val)
            .map(verdi -> "J".equals(verdi) || "1".equals(verdi))
            .orElse(null);
    }
}

