package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

public enum VedtaksbrevType {
    ORDINÆR,
    FRITEKST_FEILUTBETALING_BORTFALT; // start på vedtak er berre fritekst, og ingen periodeavsnitt

    @Converter(autoApply = true)
    public static class verdiConverter implements AttributeConverter<VedtaksbrevType, String> {
        @Override
        public String convertToDatabaseColumn(VedtaksbrevType attribute) {
            return attribute == null ? null : attribute.name();
        }

        @Override
        public VedtaksbrevType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : VedtaksbrevType.valueOf(dbData);
        }
    }
}
