package no.nav.foreldrepenger.tilbakekreving.k9sak.klient;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;

public class K9HenvisningKonvertererTest {

    @Test
    public void skal_konverterer_til_og_fra_uuid() {
        UUID uuid = UUID.fromString("28a90500-ee6b-4fcf-84e1-ab0c8e982455");
        Henvisning henvisning = K9HenvisningKonverterer.uuidTilHenvisning(uuid);
        Assertions.assertThat(henvisning).isEqualTo(new Henvisning("KKkFAO5rT8+E4asMjpgkVQ"));
        UUID gjenskapt = K9HenvisningKonverterer.henvisningTilUuid(henvisning);
        Assertions.assertThat(gjenskapt).isEqualTo(uuid);
    }

    @Test
    public void skal_konverterere_til_og_fra_uuid_random() {
        UUID uuid = UUID.randomUUID();
        Henvisning henvisning = K9HenvisningKonverterer.uuidTilHenvisning(uuid);
        UUID gjenskapt = K9HenvisningKonverterer.henvisningTilUuid(henvisning);
        Assertions.assertThat(gjenskapt).isEqualTo(uuid);
    }
}
