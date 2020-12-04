package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;

public class HentgrunnlagStegTest extends FellesTestOppsett {

    @Test
    public void utførSteg_medUtenAksjonspunkter() {
        BehandlingÅrsak.Builder behandlingÅrsakBuilder = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_KLAGE_KA).medOriginalBehandling(behandling);
        Behandling revurdering = lagBehandling(behandlingÅrsakBuilder);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(revurdering);

        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås));
        assertThat(stegResultat).isNotNull();
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(stegResultat.getAksjonspunktListe()).isEmpty();
    }

    private HentgrunnlagSteg steg() {
        return new HentgrunnlagSteg(prosessTaskRepository, behandlingRepository);
    }
}
