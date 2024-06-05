package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class InntektskategoriKlassekodeMapperTest {

    @Test
    void til_map_funksjon_skal_fungere() {
        assertThat(InntektskategoriKlassekodeMapper.tilMap("A", 3)).isEqualTo(Map.of("A", 3));
        assertThat(InntektskategoriKlassekodeMapper.tilMap("A", 3, "B", 7)).isEqualTo(Map.of("A", 3, "B", 7));

        Map<Integer, String> størreMap = InntektskategoriKlassekodeMapper.tilMap(
            1, "en",
            2, "to",
            3, "tre",
            4, "fire",
            5, "fem",
            6, "seks",
            7, "sju",
            8, "åtte",
            9, "ni",
            10, "ti",
            11, "elleve");
        assertThat(størreMap).hasSize(11);
        assertThat(størreMap.get(1)).isEqualTo("en");
        assertThat(størreMap.get(10)).isEqualTo("ti");
    }
}
