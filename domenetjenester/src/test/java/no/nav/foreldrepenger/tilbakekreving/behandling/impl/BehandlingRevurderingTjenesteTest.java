package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
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
import no.nav.vedtak.exception.FunksjonellException;

class BehandlingRevurderingTjenesteTest extends FellesTestOppsett {

    private VergeRepository vergeRepository;

    @BeforeEach
    void setUp() {
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null));
        revurderingTjeneste = new BehandlingRevurderingTjeneste(repoProvider, behandlingskontrollTjeneste);
        vergeRepository = repoProvider.getVergeRepository();
    }

    @Test
    void opprettRevurdering_nårTbkBehandlingErIkkeAvsluttet() {
        assertThatThrownBy(() -> revurderingTjeneste.opprettRevurdering(behandling.getId(), BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR))
                .isInstanceOf(FunksjonellException.class)
                .hasMessageContaining("FPT-663487");

    }

    @Test
    void opprettRevurdering_nårTbkBehandlingErAvsluttet() {
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

        var historikkinnslager = historikkinnslagRepository.hent(revurdering.getId());
        assertThat(historikkinnslager).hasSize(1);
        var historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getTittel()).isEqualTo("Tilbakekreving revurdering opprettet");
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
    }

    @Test
    void opprettRevurdering_nårTbkBehandlingErAvsluttet_medverge() {
        VergeEntitet vergeEntitet = VergeEntitet.builder().medGyldigPeriode(FOM, TOM)
                .medNavn("John Doe")
                .medKilde(KildeType.FPTILBAKE.name())
                .medVergeAktørId(behandling.getAktørId())
                .medBegrunnelse("test")
                .medVergeType(VergeType.ANNEN_F).build();
        vergeRepository.lagreVergeInformasjon(behandling.getId(), vergeEntitet);
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
