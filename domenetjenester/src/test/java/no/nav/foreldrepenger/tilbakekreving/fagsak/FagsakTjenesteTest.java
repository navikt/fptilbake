package no.nav.foreldrepenger.tilbakekreving.fagsak;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class FagsakTjenesteTest extends FellesTestOppsett {

    @Test
    public void skal_opprettFagsak_med_alleredeFinnes_fagsak() {
        Fagsak fagsak = fagsakTjeneste.opprettFagsak(saksnummer, aktørId, FagsakYtelseType.FORELDREPENGER, Språkkode.DEFAULT);
        assertThat(fagsak).isNotNull();
        // vi må sammenligne det med fagsak fra behandling fordi fagsakId generert automatisk
        assertThat(fagsak.getId()).isEqualTo(behandling.getFagsakId());
        fellesAssert(fagsak,saksnummer);
    }

    @Test
    public void skal_opprettFagsak_med_ingen_fagsak_finnes() {
        Saksnummer nyeSaksnummer = new Saksnummer("100001");
        Fagsak fagsak = fagsakTjeneste.opprettFagsak(nyeSaksnummer, aktørId, FagsakYtelseType.FORELDREPENGER, Språkkode.DEFAULT);
        assertThat(fagsak).isNotNull();
        assertThat(fagsak.getId()).isNotEqualTo(behandling.getFagsakId());
        fellesAssert(fagsak,nyeSaksnummer);
    }

    private void fellesAssert(Fagsak fagsak,Saksnummer saksnummer) {
        assertThat(fagsak.getStatus()).isEqualByComparingTo(FagsakStatus.OPPRETTET);
        assertThat(fagsak.getFagsakYtelseType()).isEqualByComparingTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(fagsak.getSaksnummer()).isEqualTo(saksnummer);
        assertThat(fagsak.getAktørId()).isEqualTo(aktørId);
    }

}
