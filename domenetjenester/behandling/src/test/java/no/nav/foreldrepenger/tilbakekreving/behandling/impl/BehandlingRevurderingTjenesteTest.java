package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;

public class BehandlingRevurderingTjenesteTest extends FellesTestOppsett {

    BehandlingRevurderingTjeneste revurderingTjeneste = new BehandlingRevurderingTjeneste(repoProvider);
    BehandlingRepository behandlingRepository = repoProvider.getBehandlingRepository();

    @Test
    public void opprettRevurdering_nårTbkBehandlingErIkkeAvsluttet() {
        expectedException.expect(FunksjonellException.class);
        expectedException.expectMessage("FPT-663487");

        revurderingTjeneste.opprettRevurdering(SAKSNUMMER, EKSTERN_BEHANDLING_UUID, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR.getKode());
    }

    @Test
    public void opprettRevurdering_nårBehandlingÅrsakErUgyldig() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-314678");

        revurderingTjeneste.opprettRevurdering(SAKSNUMMER, EKSTERN_BEHANDLING_UUID, null);
    }

    @Test
    public void opprettRevurdering_nårSaksnummerErUgyldig() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-429884");

        revurderingTjeneste.opprettRevurdering(null, EKSTERN_BEHANDLING_UUID, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR.getKode());
    }

    @Test
    public void opprettRevurdering_nårTbkBehandlingErAvsluttet() {
        BEHANDLING.avsluttBehandling();
        BehandlingLås behandlingLås = repoProvider.getBehandlingRepository().taSkriveLås(BEHANDLING);
        behandlingRepository.lagre(BEHANDLING, behandlingLås);

        Behandling revurdering = revurderingTjeneste.opprettRevurdering(SAKSNUMMER, EKSTERN_BEHANDLING_UUID, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR.getKode());
        assertThat(revurdering).isNotNull();
        assertThat(revurdering.getFagsakId()).isNotNull();
        assertThat(revurdering.getStatus()).isEqualByComparingTo(BehandlingStatus.OPPRETTET);
        assertThat(revurdering.getType()).isEqualByComparingTo(BehandlingType.REVURDERING_TILBAKEKREVING);

        assertThat(revurdering.getBehandlingÅrsaker()).isNotEmpty();
        BehandlingÅrsak behandlingÅrsak = revurdering.getBehandlingÅrsaker().get(0);
        assertThat(behandlingÅrsak.getBehandlingÅrsakType()).isEqualByComparingTo(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);

        assertThat(revurdering.getAksjonspunkter()).isNotEmpty();
        Aksjonspunkt aksjonspunkt = revurdering.getAksjonspunkter().iterator().next();
        assertThat(aksjonspunkt.getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING);

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(revurdering.getId());
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(1);

        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.REVURD_OPPR);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
    }

    @Test
    public void kanOppretteRevurdering() {

    }
}
