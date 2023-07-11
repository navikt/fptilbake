package no.nav.foreldrepenger.tilbakekreving.behandlingslager;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum SaksbehandlingType {

    ORDINÆR, AUTOMATISK_IKKE_INNKREVING_LAVT_BELØP;

    @Converter(autoApply = true)
    public static class verdiConverter implements AttributeConverter<SaksbehandlingType, String> {
        @Override
        public String convertToDatabaseColumn(SaksbehandlingType attribute) {
            return attribute == null ? null : attribute.name();
        }

        @Override
        public SaksbehandlingType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : SaksbehandlingType.valueOf(dbData);
        }
    }
}
