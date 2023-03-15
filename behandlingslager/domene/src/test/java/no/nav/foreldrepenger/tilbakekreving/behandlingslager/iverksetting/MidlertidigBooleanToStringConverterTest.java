package no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MidlertidigBooleanToStringConverterTest {

    private MidlertidigBooleanToStringConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MidlertidigBooleanToStringConverter();
    }

    @Test
    void convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(Boolean.TRUE)).isEqualTo("J");
        assertThat(converter.convertToDatabaseColumn(Boolean.FALSE)).isEqualTo("N");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToEntityAttribute() {
        assertThat(converter.convertToEntityAttribute("J")).isTrue();
        assertThat(converter.convertToEntityAttribute("1")).isTrue();
        assertThat(converter.convertToEntityAttribute("N")).isFalse();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
