package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BehandlingRevurderingTjenesteTest extends FellesTestOppsett {

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repoProvider, behandlingskontrollTjeneste, mockHistorikkTjeneste);

    @Test
    public void opprettRevurdering_nårTbkBehandlingErIkkeAvsluttet() {
        expectedException.expect(FunksjonellException.class);
        expectedException.expectMessage("FPT-663487");

        revurderingTjeneste.opprettRevurdering(saksnummer, eksternBehandlingUuid, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR, REVURDERING_BEHANDLING_TYPE);
    }

    @Test
    public void opprettRevurdering_nårSaksnummerErUgyldig() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-429884");

        revurderingTjeneste.opprettRevurdering(null, eksternBehandlingUuid, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR, REVURDERING_BEHANDLING_TYPE);
    }

    @Test
    public void opprettRevurdering_nårTbkBehandlingErAvsluttet() {
        behandling.avsluttBehandling();
        BehandlingLås behandlingLås = repoProvider.getBehandlingRepository().taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        Behandling revurdering = revurderingTjeneste.opprettRevurdering(saksnummer, eksternBehandlingUuid, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR, REVURDERING_BEHANDLING_TYPE);
        assertThat(revurdering).isNotNull();
        assertThat(revurdering.getFagsakId()).isNotNull();
        assertThat(revurdering.getStatus()).isEqualByComparingTo(BehandlingStatus.OPPRETTET);
        assertThat(revurdering.getType()).isEqualByComparingTo(REVURDERING_BEHANDLING_TYPE);
        assertThat(revurdering.getBehandlendeEnhetId()).isNotEmpty();
        assertThat(revurdering.getBehandlendeEnhetNavn()).isNotEmpty();

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
    public void kan_revurdering_ikke_opprettes_når_behandling_er_henlagt() {
        henleggBehandlingTjeneste.henleggBehandlingManuelt(behandling.getId(), BehandlingResultatType.HENLAGT_FEILOPPRETTET, "");
        assertThat(revurderingTjeneste.kanOppretteRevurdering(eksternBehandlingUuid)).isFalse();
    }

}
