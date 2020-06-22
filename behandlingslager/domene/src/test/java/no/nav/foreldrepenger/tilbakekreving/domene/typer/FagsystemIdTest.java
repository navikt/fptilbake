package no.nav.foreldrepenger.tilbakekreving.domene.typer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class FagsystemIdTest {
    @Test
    public void skal_håndtere_gammelt_format() {
        String fagsakId = "141243251101";

        FagsystemId fagsystemId = FagsystemId.parse(fagsakId);
        assertThat(fagsystemId.getSaksnummer()).isEqualTo(new Saksnummer("141243251"));
        assertThat(fagsystemId.getLøpenummer()).isEqualTo(101);
        assertThat(fagsystemId.toString()).isEqualTo("141243251101");
    }

    @Test
    public void skal_håndtere_nytt_format() {
        String fagsakId = "SEGAB-1";

        FagsystemId fagsystemId = FagsystemId.parse(fagsakId);
        assertThat(fagsystemId.getSaksnummer()).isEqualTo(new Saksnummer("SEGAB"));
        assertThat(fagsystemId.getLøpenummer()).isEqualTo(1);
        assertThat(fagsystemId.toString()).isEqualTo("SEGAB-1");
    }

    @Test
    public void skal_håndtere_nytt_format_også_med_bindestrek() {
        String saksnummer = "FAG-FP-123";
        String fagsakId = "FAG-FP-123-1";

        FagsystemId fagsystemId = FagsystemId.parse(fagsakId);
        assertThat(fagsystemId.getSaksnummer()).isEqualTo(new Saksnummer(saksnummer));
        assertThat(fagsystemId.getLøpenummer()).isEqualTo(1);
        assertThat(fagsystemId.toString()).isEqualTo(fagsakId);
    }
}
