package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.vedtak.exception.FunksjonellException;

public class BehandlingRevurderingTjenesteTest extends FellesTestOppsett {

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repoProvider, prosessTaskRepository, behandlingskontrollTjeneste, mockHistorikkTjeneste);
    private VergeRepository vergeRepository = repoProvider.getVergeRepository();

    @Test
    public void opprettRevurdering_nårTbkBehandlingErIkkeAvsluttet() {
        expectedException.expect(FunksjonellException.class);
        expectedException.expectMessage("FPT-663487");

        revurderingTjeneste.opprettRevurdering(behandling.getId(), BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);
    }

    @Test
    public void opprettRevurdering_nårTbkBehandlingErAvsluttet() {
        behandling.avsluttBehandling();
        BehandlingLås behandlingLås = repoProvider.getBehandlingRepository().taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        Behandling revurdering = revurderingTjeneste.opprettRevurdering(behandling.getId(), BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);
        assertThat(revurdering).isNotNull();
        assertThat(revurdering.getFagsakId()).isNotNull();
        assertThat(revurdering.getStatus()).isEqualByComparingTo(BehandlingStatus.OPPRETTET);
        assertThat(revurdering.getType()).isEqualByComparingTo(REVURDERING_BEHANDLING_TYPE);
        assertThat(revurdering.getBehandlendeEnhetId()).isNotEmpty();
        assertThat(revurdering.getBehandlendeEnhetNavn()).isNotEmpty();
        assertThat(vergeRepository.finnVergeInformasjon(revurdering.getId())).isEmpty();

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

    @Test
    public void opprettRevurdering_nårTbkBehandlingErAvsluttet_medverge() {
        VergeEntitet vergeEntitet = VergeEntitet.builder().medGyldigPeriode(FOM,TOM)
            .medNavn("John Doe")
            .medKilde(KildeType.FPTILBAKE.name())
            .medVergeAktørId(behandling.getAktørId())
            .medBegrunnelse("test")
            .medVergeType(VergeType.ANNEN_F).build();
        vergeRepository.lagreVergeInformasjon(behandling.getId(),vergeEntitet);
        behandling.avsluttBehandling();
        BehandlingLås behandlingLås = repoProvider.getBehandlingRepository().taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        Behandling revurdering = revurderingTjeneste.opprettRevurdering(behandling.getId(), BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);
        assertThat(revurdering).isNotNull();
        assertThat(revurdering.getFagsakId()).isNotNull();
        assertThat(revurdering.getStatus()).isEqualByComparingTo(BehandlingStatus.OPPRETTET);
        assertThat(revurdering.getType()).isEqualByComparingTo(REVURDERING_BEHANDLING_TYPE);
        assertThat(revurdering.getBehandlendeEnhetId()).isNotEmpty();
        assertThat(revurdering.getBehandlendeEnhetNavn()).isNotEmpty();
        Optional<VergeEntitet> verge = vergeRepository.finnVergeInformasjon(revurdering.getId());
        assertThat(verge).isNotEmpty();
        vergeEntitet = verge.get();
        assertThat(vergeEntitet.getVergeAktørId()).isEqualTo(revurdering.getAktørId());
        assertThat(vergeEntitet.getVergeType()).isEqualByComparingTo(VergeType.ANNEN_F);
        assertThat(vergeEntitet.getGyldigFom()).isEqualTo(FOM);
        assertThat(vergeEntitet.getGyldigTom()).isEqualTo(TOM);
    }

}
